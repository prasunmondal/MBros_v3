package com.tech4bytes.mbrosv3.AppUsers

data class AppUsersModel(val id: String, val timestamp: String, val device_id: String, var roles: String, var permissions: String) {

    companion object {

        fun isEligibleToViewHiddenDue(): Boolean {
            return true
        }
    }

    override fun toString(): String {
        return "AppUsersModel(id='$id', timestamp='$timestamp', device_id='$device_id', roles='$roles', permissions='$permissions')"
    }
}