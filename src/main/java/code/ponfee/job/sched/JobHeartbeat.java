package code.ponfee.job.sched;

import static code.ponfee.commons.util.QuartzUtils.getNextExecTime;
import static code.ponfee.job.common.Constants.IP_ADDRESS;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import code.ponfee.commons.util.SpringContextHolder;
import code.ponfee.job.common.Constants;
import code.ponfee.job.dao.ISchedJobDao;
import code.ponfee.job.model.SchedJob;

/**
 * 心跳检测中心
 * @author fupf
 */
@Component
public class JobHeartbeat {

    private static Logger logger = LoggerFactory.getLogger(JobHeartbeat.class);
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    @Resource
    private ISchedJobDao schedJobDao;


    /**
     * 心跳频率：1秒/次
     */
    @Scheduled(fixedRate = 1000)
    public void run() {
        EXECUTOR_SERVICE.submit(new HeartbeatRunner(schedJobDao));
    }

    /**
     * 线程运行类
     * @author fupf
     */
    private static final class HeartbeatRunner implements Runnable {
        private final ISchedJobDao schedJobDao;

        HeartbeatRunner(ISchedJobDao schedJobDao) {
            this.schedJobDao = schedJobDao;
        }

        @Override
        public void run() {
            // 负载均衡控制(least active load balance)
            // 查看排名：有序集成员按score值递增(从小到大)顺序排列，排名以0为底，score值最小的成员排名为0
            if (schedJobDao.incrAndRank(IP_ADDRESS, 0) > 0) {
                return;
            }

            for (int jobId : schedJobDao.listJobIds()) {
                // 占位符
                if (Constants.PLACEHOLDER.equals(jobId)) {
                    continue;
                }

                // 获取调度实体
                SchedJob job = schedJobDao.get(jobId);
                if (job == null) {
                    logger.error("schedule job not found[{}].", jobId);
                    continue;
                } else if (job.getStatus() == SchedJob.STATUS_STOP) {
                    logger.error("schedule job was stop[{}].", jobId);
                    continue;
                }

                // 获取下次执行时间
                Date now = new Date();
                if (job.getNextSchedTime() == null) {
                    // 获取基准时间点
                    Date begin = null;
                    if (!job.getRecoverySupport() || job.getLastSchedTime() == null) {
                        begin = now; // 不支持恢复执行或从未执行
                    } else {
                        begin = job.getLastSchedTime();
                    }

                    // 获取下一次的执行时间点
                    Date date = getNextExecTime(job.getCronExpression(), begin);
                    if (date == null
                        || (job.getStartTime() != null && date.before(job.getStartTime()))
                        || (job.getEndTime() != null && date.after(job.getEndTime()))) {
                        continue; // 不符合可执行的时间范围则跳过
                    }
                    job.setNextSchedTime(date);
                } else if (job.getNextSchedTime().after(now)) {
                    continue; // 还未到执行时间点则跳过
                }

                // 判断是否需要做串行执行
                if (!job.getConcurrentSupport() && job.getIsExecuting()
                    && (now.getTime() - job.getExecTimeMillis()) < clockdiff(job.getLastSchedTime(), job.getNextSchedTime())) {
                    continue; // 如果 [不支持并发] && [正在执行] && [还未到达防止死锁的超时时间] 则跳过
                }

                // 尝试获取执行锁
                if (tryAcquire(job, now)) {
                    schedJobDao.incrAndRank(IP_ADDRESS, job.getScore());
                    SpringContextHolder.getBean(JobExecutor.class).execute(job);
                    break; // 获取到新任务后就不再执行其它任务
                }
            }
        }

        /**
         * 尝试获取执行锁
         * @param  job
         * @return boolean
         */
        private boolean tryAcquire(SchedJob job, Date date) {
            Date schedTime = job.getNextSchedTime();
            if (schedTime.after(date)) {
                // 先更新，但不执行
                job.setIsExecuting(false);
                job.setExecTimeMillis(null);
            } else {
                job.setIsExecuting(true); // 执行
                job.setExecTimeMillis(date.getTime());
                job.setLastSchedTime(schedTime); // 本次执行后变为上一次执行时间
                job.setLastSchedServer(IP_ADDRESS); // 执行服务器
                job.setNextSchedTime(getNextExecTime(job.getCronExpression(), schedTime)); // 更新下一次执行时间点
            }

            // 先获取锁再判断时间
            return schedJobDao.tryAcquire(job) && !schedTime.after(date);
        }

        /**
         * 时间差
         * @param from
         * @param to
         * @return
         */
        private long clockdiff(Date from, Date to) {
            return to.getTime() - from.getTime();
        }
    }

}
