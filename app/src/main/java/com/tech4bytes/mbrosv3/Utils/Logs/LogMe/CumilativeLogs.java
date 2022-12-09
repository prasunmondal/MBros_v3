package com.tech4bytes.mbrosv3.Utils.Logs.LogMe;

public class CumilativeLogs {
    static String appendingLogs = "";
    static String appendingString = "";

    public static void appendLog(String msg) {
        if (!appendingLogs.isEmpty()) {
            appendingLogs += "\n";
        }
        appendingLogs += LogUtils.getPrefix() + msg;
    }

    public static void appendString(String msg) {
        if (!appendingString.isEmpty()) {
            appendingString += "\n";
        }
        appendingString += msg;
    }

    public static void newSessionAddLog(String msg, boolean postToSheet) {
        commitLogs(postToSheet);
        appendingLogs = msg;
    }

    public static void newSessionAddString(String msg, boolean postToSheet) {
        commitString(postToSheet);
        appendingString = msg;
    }

    public static void commit(boolean postToSheet) {
        commitLogs(postToSheet);
        commitString(postToSheet);
    }

    public static void commitString(boolean postToSheet) {
        if (!appendingString.isEmpty()) {
            LogMe.log(appendingString, true, postToSheet);
        }
        appendingString = "";
    }

    public static void commitLogs(boolean postToSheet) {
        if (!appendingLogs.isEmpty()) {
            LogMe.log(appendingLogs, false, postToSheet);
        }
        appendingLogs = "";
    }
}
