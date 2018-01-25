package code.ponfee.job.dao.impl;

import static code.ponfee.job.model.SchedJob.STATUS_START;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import javax.annotation.Resource;

import org.springframework.stereotype.Repository;

import code.ponfee.commons.jedis.JedisClient;
import code.ponfee.commons.jedis.JedisLock;
import code.ponfee.commons.math.Numbers;
import code.ponfee.commons.model.Page;
import code.ponfee.commons.model.PageHandler;
import code.ponfee.commons.util.SpringContextHolder;
import code.ponfee.job.common.Constants;
import code.ponfee.job.dao.ISchedJobDao;
import code.ponfee.job.dao.cache.SchedJobCached;
import code.ponfee.job.dao.mapper.SchedJobMapper;
import code.ponfee.job.dao.mapper.SchedLogMapper;
import code.ponfee.job.model.SchedJob;
import code.ponfee.job.model.SchedLog;

/**
 * sched job dao实现类
 * @author fupf
 */
@Repository
public class SchedJobDaoImpl implements ISchedJobDao {

    private static final String KEY_PREFIX = Constants.CACHE_KEY_PREFIX + "dao:";

    @Resource
    private SchedJobMapper jobMapper;

    @Resource
    private SchedLogMapper logMapper;

    @Resource
    private SchedJobCached cached;

    @Override
    public boolean add(SchedJob job) {
        boolean flag = jobMapper.insert(job) == 1;
        if (flag && job.getStatus() == STATUS_START) {
            cached.setJobIds(job.getId());
        }
        return flag;
    }

    @Override
    public boolean update(SchedJob job) {
        boolean flag = jobMapper.update(job) == 1;
        if (flag) {
            cached.delSchedJob(job.getId());
            if (job.getStatus() == STATUS_START) {
                cached.setJobIds(job.getId());
            } else {
                cached.remJobId(job.getId());
            }
        }
        return flag;
    }

    @Override
    public boolean toggle(SchedJob job) {
        boolean flag = jobMapper.updateStatus(job) == 1;
        if (flag) {
            cached.delSchedJob(job.getId());
            if (job.getStatus() == STATUS_START) {
                cached.setJobIds(job.getId());
            } else {
                cached.remJobId(job.getId());
            }
        }
        return flag;
    }

    @Override
    public SchedJob get(int jobId) {
        SchedJob job = cached.getSchedJob(jobId);
        if (job == null) {
            Lock lock = prepareLock(KEY_PREFIX + "get:" + jobId);
            lock.lock();
            try {
                job = cached.getSchedJob(jobId);
                if (job == null) {
                    // 缓存未命中，从库中加载
                    job = jobMapper.get(jobId);
                    if (job != null) {
                        cached.setSchedJob(job);
                    }
                }
            } finally {
                lock.unlock();
            }
        }
        return job;
    }

    @Override
    public boolean delete(int jobId, int version) {
        boolean flag = jobMapper.delete(jobId, version) == 1;
        if (flag) {
            cached.remJobId(jobId);
            cached.delSchedJob(jobId);
        }
        return flag;
    }

    @Override
    public Page<SchedJob> queryJobsForPage(Map<String, ?> params) {
        PageHandler.NORMAL.handle(params);
        return new Page<>(jobMapper.query4list(params));
    }

    @Override
    public Collection<Integer> listJobIds() {
        Collection<Integer> ids = cached.getJobIds();
        if (ids == null || ids.isEmpty()) {
            Lock lock = prepareLock(KEY_PREFIX + "list-job-ids");
            lock.lock();
            try {
                ids = cached.getJobIds();
                if (ids == null || ids.isEmpty()) {
                    ids = jobMapper.listJobIds(SchedJob.STATUS_START);
                    if (ids.isEmpty()) {
                        ids.add(Constants.PLACEHOLDER); // 添加占位符
                    }
                    cached.setJobIds(ids.toArray(new Integer[ids.size()]));
                }
            } finally {
                lock.unlock();
            }
        }

        List<Integer> list = new ArrayList<>(ids);
        Collections.shuffle(list);
        return list;
    }

    @Override
    public boolean tryAcquire(SchedJob job) {
        boolean flag = jobMapper.tryAcquire(job) == 1;
        if (flag) {
            cached.delSchedJob(job.getId());
        }
        return flag;
    }

    @Override
    public boolean doneExecution(SchedJob job) {
        boolean flag = jobMapper.doneExecution(job) == 1;
        if (flag) {
            cached.delSchedJob(job.getId());
        }
        return flag;
    }

    /**
     * 增加评分并排名
     * 按有序集成员按score值递增(从小到大)的顺序排列：排名以0为底，score值最小的成员排名为0
     */
    @Override
    public long incrAndRank(String server, int score) {
        if (!cached.incrServerScore(server, score)) {
            Lock lock = prepareLock(KEY_PREFIX + "incr-and-rank");
            lock.lock();
            try {
                if (!cached.incrServerScore(server, score)) {
                    Map<String, Double> map = convert(jobMapper.collectServersLoadBalance());
                    map.put(server, Numbers.add(map.get(server), (double) score));
                    cached.setScoreServers(map);
                }
            } finally {
                lock.unlock();
            }
        }
        return cached.getServerRank(server);
    }

    /**
     * redis分布式锁
     * @param key
     * @return
     */
    private Lock prepareLock(String key) {
        return new JedisLock(SpringContextHolder.getBean(JedisClient.class), key, 5);
    }

    /**
     * 类型转换
     * @param list
     * @return
     */
    private Map<String, Double> convert(List<Map<String, Object>> list) {
        Map<String, Double> result = new HashMap<>();
        if (list == null) {
            return result;
        }
        for (Map<String, Object> map : list) {
            BigDecimal b = (BigDecimal) map.get("scores");
            if (b == null) {
                b = new BigDecimal(0);
            }
            result.put((String) map.get("server"), b.doubleValue());
        }
        return result;
    }

    // ----------------sched log-----------------
    @Override
    public boolean recordLog(SchedLog log) {
        return recordLog(Arrays.asList(log));
    }

    @Override
    public boolean recordLog(List<SchedLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return false;
        }
        return logMapper.insert(logs) == logs.size();
    }

    @Override
    public Page<SchedLog> queryLogsForPage(Map<String, ?> params) {
        PageHandler.NORMAL.handle(params);
        return new Page<>(logMapper.query4list(params));
    }

}
