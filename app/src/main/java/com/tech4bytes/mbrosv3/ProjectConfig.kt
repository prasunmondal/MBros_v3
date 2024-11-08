package com.tech4bytes.mbrosv3

class ProjectConfig {
    companion object {
//        https://docs.google.com/spreadsheets/d/1hrbaCuDJncjEot_14WPLOpRFeBq1QbtaE1wgv50ezZ4/edit?gid=936652995#gid=936652995
        private const val isTestEnv = false
        // Version 381 on Mar 2, 2024, 9:46 AM
        const val dBServerScriptURL = "https://script.google.com/macros/s/AKfycbydFleGsskEpliffhtHRzQ8b0hmq2cupDNhDZeSsQfoFUPirqvNtyIU07GZwMZbO8Y3eA/exec"
        const val dBServerScriptURLNew = "https://script.google.com/macros/s/AKfycbzQ5qRU53UDr516Q2eiYSI4zKptkw9EB-2jvQO7GBnnmW86NOUbMzfXYHq95rPTS40-iQ/exec"


        private const val DB_FINALIZE_SHEET_ID = "11TA2pPlxqajVwkPEigNMPNfsV-12CExxmySk1OMw_v8"
        private const val DB_SHEET_ID = "1X6HriHjIE0XfAblDlE7Uf5a8JTHu00kW2SWvTFKL78w"

        private const val TEST_DB_SHEET_ID = "17C9hXWMpanM_ENlhFJevOJ1QjkBFr27EeXITBG1VWEw"
        private const val TEST_DB_FINALIZE_SHEET_ID = "1devWomGykh2XFB67qs84QVez9VutTodr7LTB6o24JOU"

        fun get_db_sheet_id(): String {
            return if (!isTestEnv)
                DB_SHEET_ID
            else
                TEST_DB_SHEET_ID
        }

        fun get_db_finalize_sheet_id(): String {
            return if (!isTestEnv)
                DB_FINALIZE_SHEET_ID
            else
                TEST_DB_FINALIZE_SHEET_ID
        }
    }
}