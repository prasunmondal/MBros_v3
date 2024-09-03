package com.tech4bytes.mbrosv3.Utils.Logs.LogMe;

import com.prasunmondal.dev.libs.contexts.AppContexts;

public class LogMe extends LogExceptions {
    static LogMe obj;

    static {
        obj = new LogMe();
    }

    public static void log(int msg) {
        log("" + msg);
    }

    public static void log(String msg) {
        if (msg == null) {
            msg = "<null string detected in log>";
        }
        log(msg, true, false);
    }

    public static void log(String msg, boolean postLogToSheet) {
        log(msg, true, postLogToSheet);
    }

    public static void toSheet(String msg) {
        log(msg, true, true);
    }

    public static void startMethod() {
        log(LogConfigurations.METHOD_START_INDICATOR);
    }

    public static void endMethod() {
        log(LogConfigurations.METHOD_END_INDICATOR);
    }

    public static void log(String msg, boolean addPrefix, boolean postLogToSheet) {
        String str = msg;
        if (addPrefix) {
            String prefixString = LogUtils.getPrefix();
            str = prefixString + msg.replace("\n", "\n" + prefixString);
        }
        str = LogUtils.putOffset(str);
        System.out.println(str);
        if (postLogToSheet) {
            LogToSheet.logs.post(str, AppContexts.get());
        }
    }
}
