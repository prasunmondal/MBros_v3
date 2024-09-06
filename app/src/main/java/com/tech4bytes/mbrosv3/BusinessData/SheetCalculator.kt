package com.tech4bytes.mbrosv3.BusinessData

import com.prasunmondal.dev.libs.contexts.AppContexts
import com.prasunmondal.dev.libs.gsheet.ContextWrapper
import com.prasunmondal.dev.libs.gsheet.clients.GScript
import com.prasunmondal.dev.libs.gsheet.clients.GSheetSerialized
import com.tech4bytes.mbrosv3.ProjectConfig

class SheetCalculator(var is_khata_green: String) : java.io.Serializable

object SheetCalculatorUtil: GSheetSerialized<SheetCalculator>(
    context = ContextWrapper(AppContexts.get()),
    sheetId = ProjectConfig.get_db_sheet_id(),
    tabName = "syncSheetCalculator",
    modelClass = SheetCalculator::class.java
) {
    fun isKhataGreen(useCache: Boolean): Boolean {
        // TODO: optimize - just uncomment the below line and delete the rest
//        return fetchAll().execute(useCache)[0].is_khata_green.toBoolean()


        // to be deleted once optimized
        fetchAll().queue()
        GScript.execute(false)
        return fetchAll().execute()[0].is_khata_green.toBoolean()
    }
}