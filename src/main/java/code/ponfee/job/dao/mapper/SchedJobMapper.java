package code.ponfee.job.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import code.ponfee.job.model.SchedJob;

/**
 * sched job mapper
 * @author fupf
 */
public interface SchedJobMapper {

    int insert(SchedJob job);

    int update(SchedJob job);

    int updateStatus(SchedJob job);

    int delete(@Param("jobId") int id, @Param("version") int vs);

    SchedJob get(int jobId);

    List<SchedJob> query4list(Map<String, ?> params);

    List<Integer> listJobIds(Integer status);

    int tryAcquire(SchedJob job);

    int doneExecution(SchedJob job);

    List<Map<String, Object>> collectServersLoadBalance();
}
