package com.tech4bytes.mbrosv3.OneShot.Delivery

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.tech4bytes.mbrosv3.AppUsers.Authorization.ActivityAuth.UserRoleUtils
import com.tech4bytes.mbrosv3.Login.ActivityLogin
import com.tech4bytes.mbrosv3.R

class Login : AppCompatActivity() {

    companion object {
        var enteredLoginPIN = ""

        fun getLoginPIN(): String {
            return enteredLoginPIN
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Thread {
            UserRoleUtils.getUserRoles()
            runOnUiThread {
                Toast.makeText(this, "Device List Downloaded", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun checkPINValidity() {

    }

    fun onClickLoginBtn(view: View) {
        enteredLoginPIN = findViewById<EditText>(R.id.login_pin_textfield).text.toString()
        val switchActivityIntent = Intent(this, ActivityLogin::class.java)
        startActivity(switchActivityIntent)
    }
}