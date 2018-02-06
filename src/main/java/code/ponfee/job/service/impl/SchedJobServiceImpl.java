package code.ponfee.job.service.impl;

import static code.ponfee.commons.util.QuartzUtils.getNextExecTime;

import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import code.ponfee.commons.constrain.Constraint;
import code.ponfee.commons.constrain.Constraint.Tense;
import code.ponfee.commons.constrain.Constraints;
import code.ponfee.commons.log.LogAnnotation;
import code.ponfee.commons.math.Numbers;
import code.ponfee.commons.model.Page;
import code.ponfee.commons.model.Result;
import code.ponfee.commons.model.ResultCode;
import code.ponfee.commons.util.Dates;
import code.ponfee.commons.util.SpringContextHolder;
import code.ponfee.job.common.JobResultCode;
import code.ponfee.job.dao.ISchedJobDao;
import code.ponfee.job.dao.cache.SchedJobCached;
import code.ponfee.job.model.SchedJob;
import code.ponfee.job.model.SchedLog;
import code.ponfee.job.sched.JobExecutor;
import code.ponfee.job.sched.handler.JobHandler;
import code.ponfee.job.sched.handler.JobHandlerLoader;
import code.ponfee.job.service.ISchedJobService;

/**
 * 任务调度服务
 * @author fupf
 */
@Service("schedJobService")
public class SchedJobServiceImpl implements ISchedJobService {

    private @Resource ISchedJobDao schedJobDao;
    private @Resource JobExecutor jobExecutor;

    @LogAnnotation
    @Constraints({ @Constraint(field = "status", notNull = false, series = { 0, 1 }) })
    public @Override Result<Page<SchedJob>> queryJobsForPage(Map<String, ?> params) {
        return Result.success(schedJobDao.queryJobsForPage(params));
    }

    @LogAnnotation
    @Constraints({
        @Constraint(field = "name", notBlank = true, maxLen = 255),
        @Constraint(field = "cronExpression", notBlank = true, maxLen = 255),
        @Constraint(field = "handler", notBlank = true),
        @Constraint(field = "status", series = { 0, 1 }),
        @Constraint(field = "score", notNull = false, min = 0, max = 100),
        @Constraint(field = "execParams", notNull = false, maxLen = 4000),
        @Constraint(field = "startTime", notNull = false, tense = Tense.FUTURE),
        @Constraint(field = "endTime", notNull = false, tense = Tense.FUTURE),
        @Constraint(field = "remark", notNull = false, maxLen = 255),
        @Constraint(field = "creatorId", min = 1),
        @Constraint(field = "creatorName", notBlank = true, maxLen = 60)
    })
    public @Override Result<Integer> addJob(SchedJob job) {
        Result<Integer> result = verifyJob(job);
        if (result != null) return result;

        Date nextSchedTime = getNextSchedTime(job.getCronExpression(), 
                                              job.getStartTime(), job.getEndTime());
        if (nextSchedTime == null) {
            return Result.failure(JobResultCode.INVALID_CRON_EXP);
        }
        job.setNextSchedTime(nextSchedTime);
        job.setCreateTime(new Date());

        // 设置更新人为创建人
        job.setModifyTime(job.getCreateTime());
        job.setModifierId(job.getCreatorId());
        job.setModifierName(job.getCreatorName());
        this.setDefault(job);

        schedJobDao.add(job);
        return Result.success(job.getId());
    }

    @LogAnnotation
    @Constraints({ @Constraint(notBlank = true) })
    public @Override Result<SchedJob> getJob(int jobId) {
        return Result.success(schedJobDao.get(jobId));
    }

    @LogAnnotation
    @Constraints({
        @Constraint(index = 0, min = 1),
        @Constraint(index = 1, min = 1)
    })
    public @Override Result<Void> delJob(int jobId, int version) {
        if (schedJobDao.delete(jobId, version)) return Result.success();
        else return Result.failure(ResultCode.OPS_CONFLICT);
    }

    @LogAnnotation
    @Constraints({
        @Constraint(field = "id", min = 1),
        @Constraint(field = "name", notBlank = true, maxLen = 255),
        @Constraint(field = "cronExpression", notBlank = true, maxLen = 255),
        @Constraint(field = "handler", notBlank = true),
        @Constraint(field = "status", series = { 0, 1 }),
        @Constraint(field = "score", notNull = false, min = 0, max = 100),
        @Constraint(field = "execParams", notNull = false, maxLen = 4000),
        @Constraint(field = "startTime", notNull = false, tense = Tense.FUTURE),
        @Constraint(field = "endTime", notNull = false, tense = Tense.FUTURE),
        @Constraint(field = "remark", notNull = false, maxLen = 255),
        @Constraint(field = "modifierId", min = 1),
        @Constraint(field = "modifierName", notBlank = true, maxLen = 60),
        @Constraint(field = "version", min = 1)
    })
    public @Override Result<Void> updJob(SchedJob job) {
        // 参数校验
        Result<Void> result = verifyJob(job);
        if (result != null) return result;

        SchedJob job0 = schedJobDao.get(job.getId());
        if (job0 == null) {
            return Result.failure(JobResultCode.JOB_NOT_FOUND);
        }

        // 判断时间表达式是否被修改
        if (!job0.getCronExpression().equals(job.getCronExpression())) {
            Date nextSchedTime = getNextSchedTime(job.getCronExpression(), 
                                                  job.getStartTime(), job.getEndTime());
            if (nextSchedTime == null) {
                return Result.failure(JobResultCode.INVALID_CRON_EXP);
            }
            job.setNextSchedTime(nextSchedTime);
        }
        job.setModifyTime(new Date());
        this.setDefault(job);

        if (schedJobDao.update(job)) return Result.success();
        else return Result.failure(ResultCode.OPS_CONFLICT);
    }

    @LogAnnotation
    @Constraints({
        @Constraint(index = 0, notBlank = true),
        @Constraint(index = 1, min = 1)
    })
    public @Override Result<Void> startJob(int jobId, int version) {
        SchedJob job = new SchedJob(jobId, version);
        job.setStatus(SchedJob.STATUS_START);
        return toggleJob(job);
    }

    @LogAnnotation
    @Constraints({
        @Constraint(index = 0, min = 1),
        @Constraint(index = 1, min = 1)
    })
    public @Override Result<Void> stopJob(int jobId, int version) {
        SchedJob job = new SchedJob(jobId, version);
        job.setStatus(SchedJob.STATUS_STOP);
        return toggleJob(job);
    }

    @LogAnnotation
    @Constraints({ @Constraint(min = 1) })
    public @Override Result<Void> triggerJob(int jobId) {
        SchedJob job = schedJobDao.get(jobId);
        if (job == null) return Result.failure(JobResultCode.JOB_NOT_FOUND);

        if (!SpringContextHolder.getBean(SchedJobCached.class).todoTrigger(jobId)) {
            return Result.failure(JobResultCode.JOB_BEING_TRIGGER);
        }

        new Thread(new JobTriggerOnce(jobExecutor, job)).start(); // 开启线程执行任务

        return Result.success();
    }

    @LogAnnotation
    @Constraints({ @Constraint(field = "beginTime", notNull = false, tense = Tense.PAST) })
    public @Override Result<Page<SchedLog>> queryLogsForPage(Map<String, ?> params) {
        if (Dates.clockdiff((Date) params.get("beginTime"), (Date) params.get("endTime")) <= 0) {
            return Result.failure(ResultCode.ILLEGAL_ARGS.getCode(), "结束时间必须大于开始时间");
        }

        return Result.success(schedJobDao.queryLogsForPage(params));
    }

    // ----------------------------private method----------------------------
    private <T> Result<T> verifyJob(SchedJob job) {
        try {
            JobHandler handler = JobHandlerLoader.loadHandler(job.getHandler());
            if (!handler.verify(job)) {
                return Result.failure(JobResultCode.INVALID_EXEC_PARAMS);
            }
        } catch (Exception e) {
            return Result.failure(JobResultCode.INVALID_JOB_HANDLER);
        }
        return null;
    }

    private Result<Void> toggleJob(SchedJob job) {
        if (schedJobDao.toggle(job)) return Result.success();
        else return Result.failure(ResultCode.OPS_CONFLICT);
    }

    private void setDefault(SchedJob job) {
        job.setScore(Numbers.bounds(job.getScore(), 1, 100));
        if (job.getConcurrentSupport() == null) {
            job.setConcurrentSupport(true);
        }
        if (job.getRecoverySupport() == null) {
            job.setRecoverySupport(true);
        }
    }

    /**
     * 鲰下次调度时间
     * @param cronExp
     * @param startTime
     * @param endTime
     * @return
     */
    private Date getNextSchedTime(String cronExp, Date startTime, Date endTime) {
        Date date = getNextExecTime(cronExp, new Date());
        if (date == null
            || (startTime != null && date.before(startTime))
            || (endTime != null && date.after(endTime))) {
            return null;
        } else {
            return date;
        }
    }

    /**
     * 触发执行一次
     */
    private static final class JobTriggerOnce implements Runnable {
        private final JobExecutor executor;
        private final SchedJob job;

        private JobTriggerOnce(JobExecutor executor, SchedJob job) {
            this.executor = executor;
            job.setLastSchedTime(null); // 为空来标示手动触发：执行完不更新
            job.setNextSchedTime(null);
            job.setExecTimeMillis(-1L);
            this.job = job;
        }

        @Override
        public void run() {
            executor.execute(job);
        }
    }

}
