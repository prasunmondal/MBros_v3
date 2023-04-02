package com.tech4bytes.mbrosv3.AppUsers.Authorization.DataAuth

import com.tech4bytes.mbrosv3.AppUsers.RolesUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe

class AuthorizationUtils {

    companion object {
        fun getAllUserAuthorizations(): List<AuthorizationEnums> {
            val listOfRoles = mutableListOf<AuthorizationEnums>()
            val appUsersData = RolesUtils.getAppUser()

            LogMe.log("= = = = = = = = = = = = Allowed Auths = = = = = = = = = = = =")
            appUsersData!!.permissions.split(",").forEach { auth ->
                listOfRoles.add(AuthorizationEnums.valueOf(auth.trim()))
                LogMe.log(AuthorizationEnums.valueOf(auth.trim()).toString())
            }
            return listOfRoles
        }

        fun doesHaveAuthorization(auth: AuthorizationEnums): Boolean {
            getAllUserAuthorizations().forEach {
                if (it.name == auth.toString()) {
                    return true
                }
            }
            return false
        }
    }
}