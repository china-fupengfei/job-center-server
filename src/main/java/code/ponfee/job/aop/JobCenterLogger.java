package code.ponfee.job.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.log.LogAnnotation;
import code.ponfee.commons.log.LogInfo;
import code.ponfee.commons.log.LoggerAspect;

/**
 * 切面日志
 * @author: fupf
 */
@Component
@Aspect
public class JobCenterLogger extends LoggerAspect {

    @Around(value = "execution(public * code.ponfee.job.service.impl.*Impl.*(..)) && @annotation(log)", argNames = "pjp,log")
    @Override
    public Object around(ProceedingJoinPoint pjp, LogAnnotation log) throws Throwable {
        return super.around(pjp, log);
    }
    
    protected void log(LogInfo logInfo) {
        System.out.println("[LOG-INFO] "+Jsons.NORMAL.stringify(logInfo));
    }

}
