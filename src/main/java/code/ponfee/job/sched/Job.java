package code.ponfee.job.sched;

import code.ponfee.job.model.SchedJob;

/**
 * 任务接口
 * @author: fupf
 */
public interface Job {

    void execute(SchedJob job);
}
