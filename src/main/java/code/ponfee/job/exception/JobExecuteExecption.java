package code.ponfee.job.exception;

import code.ponfee.commons.exception.BasicException;

/**
 * 任务执行异常
 * @author: fupf
 */
public class JobExecuteExecption extends BasicException {
    private static final long serialVersionUID = 2625614802822753946L;

    public JobExecuteExecption() {
        super();
    }

    public JobExecuteExecption(int code) {
        super(code);
    }

    public JobExecuteExecption(String message) {
        super(message);
    }

    public JobExecuteExecption(Throwable cause) {
        super(cause);
    }

    public JobExecuteExecption(String message, Throwable cause) {
        super(message, cause);
    }

    public JobExecuteExecption(int code, String message) {
        super(code, message);
    }

    public JobExecuteExecption(int code, Throwable cause) {
        super(code, cause);
    }

    public JobExecuteExecption(int code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
