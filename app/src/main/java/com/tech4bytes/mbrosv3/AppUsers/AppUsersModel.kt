package com.tech4bytes.mbrosv3.AppUsers

data class AppUsersModel(val id: String, val timestamp: String, val device_id: String, var roles: String, var permissions: String) : java.io.Serializable {

    companion object {

    }

    override fun toString(): String {
        return "AppUsersModel(id='$id', timestamp='$timestamp', device_id='$device_id', roles='$roles', permissions='$permissions')"
    }
}