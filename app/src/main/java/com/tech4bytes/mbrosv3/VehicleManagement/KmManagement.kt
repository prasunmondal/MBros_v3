package com.tech4bytes.mbrosv3.VehicleManagement

import com.prasunmondal.postjsontosheets.clients.post.serializable.PostObject
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils

class KmManagement : java.io.Serializable {

    var timestamp: String = ""
    var prevKm = 0L
    var finalKm = 0L
    var kmRun = 0L

    constructor(finalKm: Long) {
        this.timestamp = DateUtils.getCurrentTimestamp()
        this.prevKm = 0L
        this.finalKm = finalKm
        this.kmRun = finalKm - prevKm
    }

    companion object {

        fun addToServer(finalKmObj: KmManagement) {
            PostObject.builder()
                .scriptId(ProjectConfig.dBServerScriptURL)
                .sheetId(ProjectConfig.get_db_sheet_id())
                .tabName(VehicleManagementConfig.SHEET_FINAL_KM_TAB_NAME)
                .dataObject(finalKmObj as Any)
                .build().execute()
        }
    }
}