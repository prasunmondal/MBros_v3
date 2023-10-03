package com.tech4bytes.mbrosv3.AppUsers.Authorization.ActivityAuth

import com.tech4bytes.mbrosv3.AppUsers.RolesUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

class UserRoleUtils {

    companion object {
        fun getUserRoles(): List<ActivityAuthEnums> {
            val listOfRoles = mutableListOf<ActivityAuthEnums>()
            val appUsersData = RolesUtils.getAppUser()

            LogMe.log("= = = = = = = = = = = = Allowed Roles = = = = = = = = = = = =")
            if (appUsersData == null) {
                return mutableListOf()
            }
            appUsersData!!.roles.split(",").forEach { role ->
                listOfRoles.add(ActivityAuthEnums.valueOf(role.trim()))
                LogMe.log(role.trim())
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