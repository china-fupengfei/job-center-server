package code.ponfee.job.sched.handler;

import code.ponfee.job.model.SchedJob;

/**
 * 任务调度处理器接口
 * @author fupf
 */
public interface JobHandler {

    /**
     * 任务处理
     * @param job
     */
    void handle(SchedJob job);

    /**
     * 任务参数校验
     * @param params
     * @return
     */
    boolean verifyJobParams(Object params);
}
