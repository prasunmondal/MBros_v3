package com.tech4bytes.mbrosv3

class ProjectConfig {
    companion object {
        val DB_FINALIZE_SHEET_ID = "11TA2pPlxqajVwkPEigNMPNfsV-12CExxmySk1OMw_v8"
        private val DB_SHEET_ID = "1X6HriHjIE0XfAblDlE7Uf5a8JTHu00kW2SWvTFKL78w"
        val dBServerScriptURL = "https://script.google.com/macros/s/AKfycbx1flxEcbHuTPIorKgaVF-WUbnGv3qGAfmQF67liZxNpReslzumXpWVDEgrplEHhiehiA/exec"

        fun get_db_sheet_id(): String {
            return DB_SHEET_ID
        }
    }
}