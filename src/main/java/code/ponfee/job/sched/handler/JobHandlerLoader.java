package code.ponfee.job.sched.handler;

import code.ponfee.commons.compile.exception.CompileExprException;
import code.ponfee.commons.compile.impl.JdkCompiler;
import code.ponfee.commons.compile.model.JavaSource;
import code.ponfee.commons.compile.model.RegexJavaSource;

/**
 * 任务处理类加载
 * @author fupf
 */
public final class JobHandlerLoader {

    /**
     * 加载类：类全限定名或源代码
     * @param handler
     * @return
     * @throws ReflectiveOperationException
     */
    public static JobHandler loadHandler(String handler) throws ReflectiveOperationException, CompileExprException {
        try {
            JavaSource source = new RegexJavaSource(handler);
            try {
                //return (JobHandler) new JdkCompiler().compileForce(source).newInstance(); // 编译加载
                return (JobHandler) new JdkCompiler().compile(source).newInstance(); // 编译加载
            } catch (CompileExprException ex) {
                throw ex;
            }
        } catch (Exception e) {
            return (JobHandler) Class.forName(handler).newInstance(); // 配置的是类名
        }
    }
}
