package com.tech4bytes.mbrosv3.BusinessData

import com.prasunmondal.dev.libs.contexts.AppContexts
import com.prasunmondal.dev.libs.gsheet.ContextWrapper
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
        return fetchAll(useCache).execute()[0].is_khata_green.toBoolean()
    }
}