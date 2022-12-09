package com.tech4bytes.mbrosv3.Login

data class RolesModel(val id: String, val timestamp: String, val device_id: String, var role: String) {

    override fun toString(): String {
        return "\nRolesModel(id='$id', timestamp='$timestamp', device_id='$device_id', role='$role')"
    }
}