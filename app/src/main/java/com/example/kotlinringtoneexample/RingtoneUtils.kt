package com.example.kotlinringtoneexample

import android.R
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.io.File

//https://stackoverflow.com/questions/58255532/how-to-set-a-file-as-a-ringtone-for-android-10
object RingtoneUtils {
    private val LOG_TAG = "RingtoneUtils"

    fun setRingtone(context: Context, ringtoneUri: Uri, type: Int): Boolean {
        Log.v(LOG_TAG, "Setting Ringtone to: $ringtoneUri")
        if (!hasMarshmallow()) {
            Log.v(LOG_TAG, "On a Lollipop or below device, so go ahead and change device ringtone")
            setActualRingtone(context, ringtoneUri, type)
            return true
        } else if (hasMarshmallow() && canEditSystemSettings(context)) {
            Log.v(
                LOG_TAG,
                "On a marshmallow or above device but app has the permission to edit system settings"
            )
            setActualRingtone(context, ringtoneUri, type)
            return true
        } else if (hasMarshmallow() && !canEditSystemSettings(context)) {
            Log.d(
                LOG_TAG, "On android Marshmallow and above but app does not have permission to" +
                        " edit system settings. Opening the manage write settings activity..."
            )
            startManageWriteSettingsActivity(context)
            Toast.makeText(
                context,
                "Please allow app to edit settings so your ringtone/notification can be updated",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        return false
    }

    private fun setActualRingtone(context: Context, ringtoneUri: Uri, type: Int) {
        RingtoneManager.setActualDefaultRingtoneUri(context, type, ringtoneUri)
        var message = ""
        if (type == RingtoneManager.TYPE_RINGTONE) {
            message = "ringtone_set_success"
        } else if (type == RingtoneManager.TYPE_NOTIFICATION) {
            message = "notification_set_success"
        }
        if (RingtoneManager.getActualDefaultRingtoneUri(context, type) == ringtoneUri) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                context,
                "operation_failed",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun startManageWriteSettingsActivity(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        // Passing in the app package here allows the settings app to open the exact app
        intent.data = Uri.parse("package:" + context.getApplicationContext().getPackageName())
        // Optional. If you pass in a service context without setting this flag, you will get an exception
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    private fun hasMarshmallow(): Boolean {
        // returns true if the device is Android Marshmallow or above, false otherwise
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun canEditSystemSettings(context: Context): Boolean {
        // returns true if the app can edit system settings, false otherwise
        return Settings.System.canWrite(context.getApplicationContext())
    }
}