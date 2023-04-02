package com.tech4bytes.mbrosv3.AppUsers.Authorization.ActivityAuth

import com.tech4bytes.mbrosv3.AppUsers.RolesUtils

class UserRoleUtils {

    companion object {
        fun getUserRoles(): List<ActivityAuthEnums> {
            val listOfRoles = mutableListOf<ActivityAuthEnums>()
            val appUsersData = RolesUtils.getAppUser()

            appUsersData!!.roles.split(",").forEach { role ->
                listOfRoles.add(ActivityAuthEnums.valueOf(role.trim()))
            }
            return listOfRoles
        }

        fun doesHaveRole(role: ActivityAuthEnums): Boolean {
            getUserRoles().forEach {
                if (it.name == role.toString()) {
                    return true
                }
            }
            return false
        }
    }
}