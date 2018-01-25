package code.ponfee.job.dao.cache;

import static code.ponfee.job.common.Constants.CACHE_KEY_PREFIX;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Repository;

import code.ponfee.commons.jedis.JedisClient;
import code.ponfee.commons.util.MessageFormats;
import code.ponfee.job.model.SchedJob;

/**
 * schedule job cached
 * @author fupf
 */
@Repository
public class SchedJobCached {
    private static final String SCHED_IDS_KEY = CACHE_KEY_PREFIX + "ids:key";
    private static final String SCHED_JOB_KEY = CACHE_KEY_PREFIX + "job:#{id}";
    private static final String SCHED_SCORES_KEY = CACHE_KEY_PREFIX + "scores:key";
    private static final String TRIGGER_JOB_KEY = CACHE_KEY_PREFIX + "trigger:#{jobId}";
    private static final int SCHED_CACHE_TIME = 900; // 15分钟缓存时间

    private @Resource JedisClient jedisClient;

    // -----------------------------job ids----------------------------
    public void setJobIds(Integer... ids) {
        String[] result = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            result[i] = String.valueOf(ids[i]);
        }
        jedisClient.setOps().sadd(SCHED_IDS_KEY, SCHED_CACHE_TIME, result);
    }

    public void delJobIds() {
        jedisClient.keysOps().del(SCHED_IDS_KEY);
    }

    public void remJobId(int id) {
        jedisClient.setOps().srem(SCHED_IDS_KEY, String.valueOf(id));
    }

    public Set<Integer> getJobIds() {
        Set<String> set = jedisClient.setOps().smembers(SCHED_IDS_KEY);
        if (set == null) {
            return null;
        } else if (set.isEmpty()) {
            return new HashSet<>();
        }

        Set<Integer> result = new HashSet<>(set.size());
        for (String s : set) {
            result.add(Integer.parseInt(s));
        }
        return result;
    }

    // -----------------------------schedule job----------------------------
    public void setSchedJob(SchedJob job) {
        String key = MessageFormats.format(SCHED_JOB_KEY, job.getId());
        jedisClient.valueOps().setObject(key.getBytes(), job, SCHED_CACHE_TIME);
    }

    public void delSchedJob(int jobId) {
        String key = MessageFormats.format(SCHED_JOB_KEY, jobId);
        jedisClient.keysOps().del(key);
    }

    public SchedJob getSchedJob(int jobId) {
        String key = MessageFormats.format(SCHED_JOB_KEY, jobId);
        return jedisClient.valueOps().getObject(key.getBytes(), SchedJob.class);
    }

    // -----------------------------负载均衡----------------------------
    public void setScoreServers(Map<String, Double> members) {
        jedisClient.zsetOps().zadd(SCHED_SCORES_KEY, members, SCHED_CACHE_TIME * 2);
    }

    public long getServerRank(String server) {
        Long rank = jedisClient.zsetOps().zrank(SCHED_SCORES_KEY, server);
        return rank == null ? 0 : rank;
    }

    public boolean incrServerScore(String server, int score) {
        // 正分：服务器准备执行调度；负分：服务器已完成调度
        if (/*!jedisClient.keysOps().exists(SCHED_SCORES_KEY)*/
        jedisClient.keysOps().ttl(SCHED_SCORES_KEY) < SCHED_CACHE_TIME) {
            return false;
        } else {
            jedisClient.zsetOps().zincrby(SCHED_SCORES_KEY, server, score);
            return true;
        }
    }

    // -----------------------------手动触发----------------------------
    public boolean todoTrigger(int jobId) {
        String key = MessageFormats.format(TRIGGER_JOB_KEY, jobId);
        return jedisClient.valueOps().incrBy(key, 1, 3600) == 1;
    }

    public void doneTrigger(int jobId) {
        String key = MessageFormats.format(TRIGGER_JOB_KEY, jobId);
        jedisClient.keysOps().del(key);
    }

}
