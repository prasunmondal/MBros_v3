package com.tech4bytes.mbrosv3.Utils.Logs.LogMe;

public class LogUtils {

    public static StackTraceElement getCaller() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        return stackTraceElements[getCallerIndex(stackTraceElements)];
    }

    static int getCallerIndex(StackTraceElement[] stackTraceElements) {
        int i;
        boolean enteredLogMe = false;
        for (i = 0; i < stackTraceElements.length; i++) {
            if (stackTraceElements[i].getClassName().startsWith(LogMe.class.getPackage().getName())) {
                enteredLogMe = true;
            }
            if (enteredLogMe && !stackTraceElements[i].getClassName().startsWith(LogMe.class.getPackage().getName())) {
                break;
            }
        }
        return i;
    }

    static String getPrefix() {
        StackTraceElement caller = getCaller();
        return LogConfigurations.APP_LOGS_PREFIX + getClassName(caller) + ":" + getLineNumber(caller) + "|" + getMethodName(caller) + "(): ";
    }

    static String putPrefix(String str) {
        String prefix = getPrefix();
        str = str.replace("\n", "\n" + prefix);
        return str;
    }

    static String putOffset(String str) {
        String lineOffset = lineOffset();
        str = str.replace("\n", "\n" + lineOffset);
        str = lineOffset + str;
        return str;
    }

    static String lineOffset() {
        if (!LogConfigurations.IS_LINE_OFFSET_ENABLED) {
            return "";
        }

        StringBuilder str = new StringBuilder();
        int logDepth = getdepth();
        for (int i = 0; i < logDepth; i++) {
            str.append(LogConfigurations.OFFSET_STRING_UNIT);
        }
        return str.toString();
    }

    private static String getClassName(StackTraceElement caller) {
        String className = caller.getClassName();
        return className.substring(className.lastIndexOf('.') + 1);
    }

    private static int getLineNumber(StackTraceElement caller) {
        return caller.getLineNumber();
    }

    private static String getMethodName(StackTraceElement caller) {
        String methodName = caller.getMethodName();
        if (methodName.equals("<init>")) {
            methodName = LogConfigurations.CONSTRUCTOR_INDICATOR;
        }
        return methodName;
    }

    static int getdepth() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        int i = getCallerIndex(stackTraceElements);
        int j = 0;
        for (; i < stackTraceElements.length; i++) {
            if (stackTraceElements[i].toString().startsWith("android.")) {
                return j;
            } else {
                j++;
            }
        }
        return j;
    }
}
