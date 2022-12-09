package com.tech4bytes.mbrosv3.Utils.Logs.LogMe;

public class LogStackTrace extends CumilativeLogs {
    public static void logStackTrace() {
        logStackTrace(LogConfigurations.DEFAULT_STACKTRACE_INDICATOR);
    }

    public static void logStackTrace(String msg) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StringBuilder str = new StringBuilder();
        str.append(msg + "\n");
        int i = LogUtils.getCallerIndex(stackTraceElements);
        for (; i < stackTraceElements.length; i++) {
            str.append(stackTraceElements[i].toString()).append("\n");
        }
        LogMe.log(str.toString());
    }
}
