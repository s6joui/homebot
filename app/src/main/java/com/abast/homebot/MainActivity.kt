package com.abast.homebot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.content.Intent
import android.provider.Settings
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        warningButton.setOnClickListener{
            // Opens assistant settings
            val intent = Intent(Settings.ACTION_VOICE_INPUT_SETTINGS)
            val packageManager = packageManager
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this,getString(R.string.error_no_assistant),Toast.LENGTH_LONG).show()
            }
        }

        supportFragmentManager.beginTransaction().replace(R.id.preferences_frame, HomeBotPreferenceFragment()).commit()
    }
}