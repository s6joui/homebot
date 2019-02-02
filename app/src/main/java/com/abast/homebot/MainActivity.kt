package com.abast.homebot

import android.content.ComponentName
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.content.Intent
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
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

    override fun onResume() {
        super.onResume()
        if(isAssistApp(this)){
            warningText.visibility = View.GONE
            warningButton.visibility = View.GONE
        }else{
            warningText.visibility = View.VISIBLE
            warningButton.visibility = View.VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if(item?.itemId == R.id.licenses){
            startActivity(Intent(this,LicensesActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Checks if app is set as the Assist app.
     */
    private fun isAssistApp(context: Context) : Boolean {
        val assistant = Settings.Secure.getString(context.contentResolver, "assistant")
        var isAssistApp = false
        if (assistant != null) {
            val cn = ComponentName.unflattenFromString(assistant)
            if (cn != null) {
                if (cn.packageName == context.packageName) {
                    isAssistApp = true
                }
            }
        }
        return isAssistApp
    }

}