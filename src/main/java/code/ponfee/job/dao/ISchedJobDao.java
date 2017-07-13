package code.ponfee.job.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import code.ponfee.job.model.SchedJob;
import code.ponfee.job.model.SchedLog;
import code.ponfee.commons.model.Pagination;

/**
 * sched job dao interface
 * @author fupf
 */
public interface ISchedJobDao {

    boolean add(SchedJob job);

    boolean update(SchedJob job);

    boolean toggle(SchedJob job);

    SchedJob get(int jobId);

    boolean delete(int jobId, int version);

    Pagination<SchedJob> queryJobsForPage(Map<String, ?> params);

    Collection<Integer> listJobIds();

    boolean tryAcquire(SchedJob job);

    boolean doneExecution(SchedJob job);

    long incrAndRank(String server, int score);

    /** sched log */
    boolean recordLog(SchedLog log);

    boolean recordLog(List<SchedLog> logs);

    Pagination<SchedLog> queryLogsForPage(Map<String, ?> params);
}
