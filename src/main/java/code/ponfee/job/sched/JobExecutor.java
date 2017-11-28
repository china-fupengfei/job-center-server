package code.ponfee.job.sched;

import static code.ponfee.job.common.Constants.IP_ADDRESS;

import java.util.Date;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import code.ponfee.commons.compile.exception.CompileExprException;
import code.ponfee.commons.exception.Throwables;
import code.ponfee.commons.util.SpringContextHolder;
import code.ponfee.job.common.Constants;
import code.ponfee.job.dao.ISchedJobDao;
import code.ponfee.job.dao.cache.SchedJobCached;
import code.ponfee.job.exception.JobExecuteExecption;
import code.ponfee.job.model.SchedJob;
import code.ponfee.job.model.SchedLog;
import code.ponfee.job.sched.handler.JobHandlerLoader;

/**
 * 调度执行器
 * @author fupf
 */
@Component
public class JobExecutor implements Job {
    private static Logger logger = LoggerFactory.getLogger(JobExecutor.class);

    @Resource
    private ISchedJobDao schedJobDao;

    @Override
    public void execute(SchedJob job) {
        Date start = new Date();
        Exception ex = null;
        boolean isSuccess = false;
        try {
            if (logger.isInfoEnabled()) {
                logger.info("schedule job execute begin [{}-{}]", job.getId(), job.getName());
            }
            JobHandlerLoader.loadHandler(job.getHandler()).handle(job);
            if (logger.isInfoEnabled()) {
                logger.info("schedule job execute end [{}-{}]", job.getId(), job.getName());
            }
            isSuccess = true;
        } catch (ReflectiveOperationException | CompileExprException e) {
            ex = e;
            logger.error("job handler invalid [{}-{}-{}]", job.getId(), job.getName(), job.getHandler(), e);
        } catch (JobExecuteExecption e) {
            ex = e;
            logger.error("job execute exception [{}-{}]", job.getId(), job.getName(), e);
        } catch (Exception e) {
            ex = e;
            logger.error("job execute error [{}-{}]", job.getId(), job.getName(), e);
        } finally {
            boolean isManual = (job.getLastSchedTime() == null);
            if (isManual) {
                try {
                    // 手动触发执行完成后清空缓存
                    SpringContextHolder.getBean(SchedJobCached.class).doneTrigger(job.getId());
                } catch (Exception err) {
                    logger.error("job done trigger error [{}-{}]", job.getId(), job.getName(), err);
                }
            } else {
                try {
                    schedJobDao.doneExecution(job); // 执行完成
                } catch (Exception err) {
                    logger.error("job done execution error [{}-{}]", job.getId(), job.getName(), err);
                }
                try {
                    schedJobDao.incrAndRank(IP_ADDRESS, job.getScore() * -1);
                } catch (Exception err) {
                    logger.error("job incr and rank error [{}-{}]", job.getId(), job.getName(), err);
                }
            }

            // 日志记录
            Date end = new Date();
            String params = job.getExecParams();
            Date sched = job.getLastSchedTime();

            String exception = Throwables.getStackTrace(ex);
            if (exception != null && exception.length() > Constants.MAX_ERROR_LENGTH) {
                exception = exception.substring(0, Constants.MAX_ERROR_LENGTH);
            }

            try {
                schedJobDao.recordLog(new SchedLog(IP_ADDRESS, isSuccess, isManual, job.getId(), job.getName(), params, sched, start, end, exception));
            } catch (Exception err) {
                logger.error("job record log error [{}-{}]", job.getId(), job.getName(), err);
            }
        }
    }

}
