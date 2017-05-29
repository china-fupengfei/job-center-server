package code.ponfee.job.dao.mapper;

import java.util.List;
import java.util.Map;

import code.ponfee.job.model.SchedLog;

/**
 * sched log mapper
 * @author: fupf
 */
public interface SchedLogMapper {

    int record(List<SchedLog> logs);

    List<SchedLog> query4list(Map<String, ?> params);
}
