package com.example.kotlinringtoneexample

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_home.*
import java.io.File


class HomeFragment : Fragment() {

    var TAG = "001"
    val KEY = 1
    val CODE_WRITE_SETTINGS_PERMISSION = 2

    val IS_ALARM = 1
    val IS_NOTIFICATION = 2
    val IS_RINGTONE = 3

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View? = inflater?.inflate(R.layout.fragment_home, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val file = File(Environment.getExternalStorageDirectory().toString() + "/Music/aloha.mp3")
//        val file = File(Environment.getExternalStorageDirectory().toString() + "/Download/aloha.mp3")
        val path = file.absolutePath

        btn_per.setOnClickListener(View.OnClickListener {
            requestPermission()
        })

        btn_ringtone.setOnClickListener(View.OnClickListener {
            setRingTone(path)
        })

        btn_notification.setOnClickListener(View.OnClickListener {
            setNotificationSound(path)
        })

        btn_alarm.setOnClickListener(View.OnClickListener {
            setAlarmManager(path)
        })

        btn_ringtone_contact.setOnClickListener(View.OnClickListener {
            val contact_number = "0942132785"
            setRingToneWithContactNumber(path, contact_number)
        })
    }

    fun getUriFromFile(filePath: String): Uri? {
        val folder = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA
        )
        val cursor: Cursor? = context!!.getContentResolver()
            .query(folder, projection, MediaStore.Audio.Media.DATA + "=?", arrayOf(filePath), null)
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return Uri.parse(
                        folder.toString() + File.separator + cursor.getString(
                            cursor.getColumnIndex(
                                MediaStore.Audio.Media._ID
                            )
                        )
                    )
                }
            } finally {
                cursor.close()
            }
        }
        return null
    }

    fun getOrNew(filePath: String, typeRing: Int): Uri? {
        val resolver: ContentResolver = context!!.getContentResolver()
        val file = File(filePath)
        if (file.exists()) {
            val oldUri = getUriFromFile(filePath)
            if (oldUri != null) {
                return oldUri
            } else {
                val values = ContentValues()
                values.put(MediaStore.Audio.AudioColumns.DISPLAY_NAME, file.name)
                values.put(MediaStore.Audio.AudioColumns.DATA, file.absolutePath)
                values.put(MediaStore.Audio.AudioColumns.TITLE, file.name)
                values.put(MediaStore.Audio.AudioColumns.SIZE, file.length())
                values.put(MediaStore.Audio.AudioColumns.MIME_TYPE, "audio/mp3")
                when (typeRing) {
                    IS_ALARM -> values.put(MediaStore.Audio.AudioColumns.IS_ALARM, true)
                    IS_NOTIFICATION -> values.put(
                        MediaStore.Audio.AudioColumns.IS_NOTIFICATION,
                        true
                    )
                    IS_RINGTONE -> values.put(MediaStore.Audio.AudioColumns.IS_RINGTONE, true)
                }

//                val audioCollection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                return resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
            }
        }
        return null
    }

    fun setAlarmManager(filePath: String) {
        val uri = getOrNew(filePath, IS_ALARM)
        if (uri != null) {
            RingtoneManager.setActualDefaultRingtoneUri(
                activity!!.applicationContext,
                RingtoneManager.TYPE_ALARM,
                uri
            )
        } else {
            Toast.makeText(context, "uri null", Toast.LENGTH_SHORT).show()
        }

    }

    fun setNotificationSound(filePath: String) {
        val uri = getOrNew(filePath, IS_NOTIFICATION)
        if (uri != null) {
            RingtoneManager.setActualDefaultRingtoneUri(
                activity!!.applicationContext,
                RingtoneManager.TYPE_NOTIFICATION,
                uri
            )
        } else {
            Toast.makeText(context, "uri null", Toast.LENGTH_SHORT).show()
        }
    }

    fun setRingTone(filePath: String) {
        val uri = getOrNew(filePath, IS_RINGTONE)
        if (uri != null) {
            RingtoneManager.setActualDefaultRingtoneUri(
                activity!!.applicationContext,
                RingtoneManager.TYPE_RINGTONE,
                uri
            )
        } else {
            Toast.makeText(context, "uri null", Toast.LENGTH_SHORT).show()
        }
    }

    fun setRingToneWithContactNumber(filePath: String, contactNumber: String) {
        val values = ContentValues()
        val resolver: ContentResolver = context!!.getContentResolver()
        val uri = getOrNew(filePath, IS_RINGTONE)
        if (uri != null) {
            val lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                contactNumber
            )
            val projection =
                arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY)
            val data: Cursor? = context!!.getContentResolver()
                .query(lookupUri, projection, null, null, null)
            if (data != null) {
                try {
                    if (data.moveToFirst()) {
                        // Get the contact lookup Uri
                        val contactId = data.getLong(0)
                        val lookupKey = data.getString(1)
                        val contactUri =
                            ContactsContract.Contacts.getLookupUri(contactId, lookupKey)
                        val uriString = uri.toString()
                        values.put(ContactsContract.Contacts.CUSTOM_RINGTONE, uriString)
                        resolver.update(contactUri, values, null, null).toLong()
                    }
                } finally {
                    data.close()
                }
            } else {
                Toast.makeText(context, "insert Fail", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "uri null", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("WrongConstant")
    private fun requestPermission() {
        val permission: Boolean
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission = Settings.System.canWrite(context)
            if (PermissionChecker.checkSelfPermission(
                    context!!,
                    Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED
                || PermissionChecker.checkSelfPermission(
                    context!!,
                    Manifest.permission.WRITE_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED
                || PermissionChecker.checkSelfPermission(
                    context!!,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED || PermissionChecker.checkSelfPermission(
                    context!!,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val permissions = arrayOf(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS,
                    Manifest.permission.CHANGE_CONFIGURATION,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS,
                    Manifest.permission.WRITE_SETTINGS,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                ActivityCompat.requestPermissions(activity!!, permissions, KEY)
            }
        } else {
            permission = ContextCompat.checkSelfPermission(
                context!!,
                Manifest.permission.WRITE_SETTINGS
            ) == PackageManager.PERMISSION_GRANTED
        }
        if (permission) {
            //do your code
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = Uri.parse("package:" + context!!.getPackageName())
                this.startActivityForResult(intent, CODE_WRITE_SETTINGS_PERMISSION)
            } else {
                ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf(Manifest.permission.WRITE_SETTINGS),
                    CODE_WRITE_SETTINGS_PERMISSION
                )
            }
        }
    }

    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODE_WRITE_SETTINGS_PERMISSION) {
            Log.d("TAG", "MainActivity.CODE_WRITE_SETTINGS_PERMISSION success")
            if (Settings.System.canWrite(context)) {
//                setRingtone()
                Log.d(TAG, "onActivityResult: ")
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                    .setData(Uri.parse("package:" + activity?.getPackageName()))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CODE_WRITE_SETTINGS_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //do your code
        }
    }
}