package code.ponfee.job.sched.handler;

import code.ponfee.commons.compile.exception.CompileExprException;
import code.ponfee.commons.compile.impl.JdkCompiler;
import code.ponfee.commons.compile.model.JavaSource;
import code.ponfee.commons.compile.model.JavacJavaSource;

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
    public static JobHandler loadHandler(String handler) throws ReflectiveOperationException {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(handler); // 配置的是类名
        } catch (ClassNotFoundException e) {
            JavaSource javaSource = new JavacJavaSource(handler);
            try {
                clazz = Class.forName(javaSource.getFullyQualifiedName()); // 类名加载
            } catch (ClassNotFoundException ex) {
                try {
                    clazz = new JdkCompiler().compile(javaSource); // 编译加载
                } catch (CompileExprException cee) {
                    throw e;
                }
            }
        }
        return (JobHandler) clazz.newInstance();
    }
}
