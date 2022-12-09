package com.tech4bytes.mbrosv3.Utils.Logs.LogMe;

import com.prasunmondal.lib.posttogsheets.PostToGSheet;

public class LogToSheet {

    //    public static String prodLogsSheet = "https://docs.google.com/spreadsheets/d/1bhcArLoXvN2nRPSWMpgXhLzDw8zlCWu5UhCMdk-Iu60/edit#gid=0";
    public static String devLogsSheet = "https://docs.google.com/spreadsheets/d/1DTYudd2Ax0EkBvqlgfY3h-LTJqY4uQGaJNyMIYbCSdg/edit#gid=0";

    public static String currentEnvSheet = devLogsSheet;

    public static PostToGSheet logs =
            new PostToGSheet(
                    "https://script.google.com/macros/s/AKfycbyoYcCSDEbXuDuGf0AhQjEi61ECAkl8JUv4ffNofz1yBIKfcT4/exec",
                    currentEnvSheet,
                    "logsRepo",
                    "https://docs.google.com/spreadsheets/d/1qacLjDP01fA5xxo1RNI9oGDyP6iknMQyIOPx24brJlA/edit#gid=0",
                    "template",
                    true, null
            );
}