package com.tech4bytes.mbrosv3

class ProjectConfig {
    companion object {
        private const val isTestEnv = false
        const val dBServerScriptURL = "https://script.google.com/macros/s/AKfycbzxcCWtNfeBu6ATB0zYHYRr2Rfwez5PdlyuWXbpqtTm7FSgW6eCoQP9q_43opwa-8SCww/exec"

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