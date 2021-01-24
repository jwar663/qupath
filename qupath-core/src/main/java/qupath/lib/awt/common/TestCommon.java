package qupath.lib.awt.common;

public class TestCommon {

    public static String methodCalled() {
        String methodName=null;
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        for (int i = 0; i < stacktrace.length; i++) {
            if(stacktrace[i].getMethodName().equals("method")) {
                methodName = stacktrace[i+1].getMethodName();
                break;
            }
        }
        return methodName;
    }
}
