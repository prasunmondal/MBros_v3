package com.tech4bytes.mbrosv3.AppUsers.Authorization.ActivityAuth

import com.tech4bytes.mbrosv3.AppUsers.RolesUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

class UserRoleUtils {

    companion object {
        private var userRoleMap: MutableMap<String, List<ActivityAuthEnums>> = mutableMapOf()
        fun getUserRoles(): List<ActivityAuthEnums> {
            val appUsersData = RolesUtils.getAppUser() ?: return mutableListOf()
            if (userRoleMap.containsKey(appUsersData.device_id)) {
                return userRoleMap[appUsersData.device_id]!!
            }

            val listOfRoles = mutableListOf<ActivityAuthEnums>()
            LogMe.log("= = = = = = = = = = = = Allowed Roles = = = = = = = = = = = =")
            appUsersData.roles.split(",").forEach { role ->
                listOfRoles.add(ActivityAuthEnums.valueOf(role.trim()))
                LogMe.log(role.trim())
            }
            userRoleMap[appUsersData.device_id] = listOfRoles
            return listOfRoles
        }

        fun deleteUserRolesCache() {
            userRoleMap = mutableMapOf()
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