package com.tech4bytes.mbrosv3.AppUsers.Authorization.DataAuth

import com.tech4bytes.mbrosv3.AppUsers.RolesUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

class AuthorizationUtils {

    companion object {

        fun getAllUserAuthorizationsString(): List<String> {
            val listOfRoles = mutableListOf<String>()
            val appUsersData = RolesUtils.getAppUser()

            LogMe.log("= = = = = = = = = = = = Allowed Auths = = = = = = = = = = = =")
            appUsersData!!.permissions.split(",").forEach { auth ->
                listOfRoles.add(auth.trim())
            }
            return listOfRoles
        }
        fun getAllUserAuthorizations(): List<AuthorizationEnums> {
            val listOfRoles = mutableListOf<AuthorizationEnums>()
            val appUsersData = RolesUtils.getAppUser()

            LogMe.log("= = = = = = = = = = = = Allowed Auths = = = = = = = = = = = =")
            appUsersData!!.permissions.split(",").forEach { auth ->
                try {
                    listOfRoles.add(AuthorizationEnums.valueOf(auth.trim()))
                    LogMe.log(AuthorizationEnums.valueOf(auth.trim()).toString())
                } catch (e: IllegalArgumentException) {
                    LogMe.log("Enum conversion failed. Saved as String.: " + auth.trim())
                }
            }
            return listOfRoles
        }

        fun isAuthorized(auth: AuthorizationEnums): Boolean {
            getAllUserAuthorizations().forEach {
                if (it.name == auth.toString()) {
                    return true
                }
            }
            return false
        }

        fun isAuthorized(auth: String): Boolean {
            getAllUserAuthorizationsString().forEach {
                if (it == auth) {
                    return true
                }
            }
            return false
        }
    }
}