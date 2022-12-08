package com.tech4bytes.mbrosv3.Login

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import com.tech4bytes.mbrosv3.Loading.ActivityDeliveringLoad

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class ActivityLogin : AppCompatActivity() {
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (getRole()) {
            Roles.ADMIN -> goToAdminRole()
            Roles.DELIVERY -> goToDeliveryRole()
            Roles.COLLECTOR -> goToCollectorRole()
        }
    }

    private fun goToCollectorRole() {
        TODO("Not yet implemented")
    }

    private fun goToDeliveryRole() {
        val switchActivityIntent = Intent(this, ActivityDeliveringLoad::class.java)
        startActivity(switchActivityIntent)
    }

    private fun goToAdminRole() {
        TODO("Not yet implemented")
    }

    private fun getRole(): Roles {
        return Roles.DELIVERY
    }
}