package code.ponfee.job.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import code.ponfee.commons.log.LogAnnotation;
import code.ponfee.commons.log.LogInfo;
import code.ponfee.commons.log.LogRecorder;
import code.ponfee.commons.util.ObjectUtils;

/**
 * 切面日志
 * @author fupf
 */
@Component
@Aspect
public class JobCenterLogger extends LogRecorder {

    @Around(value = "execution(public * code.ponfee.job.service.impl.*Impl.*(..)) && @annotation(log)", argNames = "pjp,log")
    public @Override Object around(ProceedingJoinPoint pjp, LogAnnotation log) throws Throwable {
        return super.around(pjp, log);
    }

    /**
     * 日志自定义处理
     */
    protected void log(LogInfo logInfo) {
        System.out.println("[log-info] " + ObjectUtils.toString(logInfo));
    }

}
