package com.tech4bytes.mbrosv3.OneShot.Delivery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
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
    }

    fun onClickLoginBtn(view: View) {
        enteredLoginPIN = findViewById<EditText>(R.id.login_pin_textfield).text.toString()
    }
}