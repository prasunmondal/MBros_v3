package com.tech4bytes.mbrosv3.Utils.Logs.LogMe;

public class LogExceptions extends LogStackTrace {

    public static void log(Exception e) {
        log(e, "");
    }

    public static void log(Exception e, String msg) {
        String str;
        if (msg == null || msg.isEmpty()) {
            msg = "";
        } else {
            msg = "\nException occurred: " + msg + "\n";
        }

        str = "< EXCEPTION START >" +
                msg +
                "\nException: " + e +
                "\nMessage: " + e.getMessage() +
                "\n--------------- Stacktrace ---------------" +
                "\n" + getStackTrace(e) +
                "< EXCEPTION END >";

        LogMe.log(LogUtils.putPrefix(str), true);
    }

    private static String getStackTrace(Exception ex) {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] st = ex.getStackTrace();
        sb.append(ex.getClass().getName()).append(": ").append(ex.getMessage()).append("\n");
        for (StackTraceElement stackTraceElement : st) {
            sb.append("\t at ").append(stackTraceElement.toString()).append("\n");
        }
        return sb.toString();
    }
}