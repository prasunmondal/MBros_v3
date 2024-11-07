package com.tech4bytes.mbrosv3.AppUsers

data class AppUsersModel(
    val id: String,
    val timestamp: String,
    val device_id: String,
    var roles: String,
    var permissions: String,
    var recommended_app_version: String
) : java.io.Serializable {

    override fun toString(): String {
        return "AppUsersModel(id='$id', timestamp='$timestamp', device_id='$device_id', roles='$roles', permissions='$permissions')"
    }
}