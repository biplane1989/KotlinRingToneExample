package com.example.kotlinringtoneexample

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
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
import java.io.*

class HomeFragment : Fragment() {

    var TAG = "001"
    val KEY = 1
    val CODE_WRITE_SETTINGS_PERMISSION = 2
    lateinit var path: String
    lateinit var result: String

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

        btn_save.setOnClickListener(View.OnClickListener {
            result = saveas()
            Log.d(TAG, "onViewCreated: result " + result)
//            val file = context!!.getExternalFilesDir("music") as File;
        })

        btn_per.setOnClickListener(View.OnClickListener {
            requestPermission()
        })

        val file = context!!.getExternalFilesDir("music") as File;
        val path = file.absolutePath + File.separator + "trungkhanh.mp3"

        val title = "aloha"

//        val path = "/data/data/com.example.kotlinringtoneexample/music/hello.mp3"
//        val path = " /data/data/com.example.kotlinringtoneexample/app_hello.mp3/hello.mp3"

        val f = File(path)
        Log.d(TAG, "onViewCreated: " + path)
        if (f.exists()) {
            Log.d(TAG, "onViewCreated: %%% " + path)
        }

        btn_ringtone.setOnClickListener(View.OnClickListener {

//            /storage/emulated/0/Android/data/com.example.kotlinringtoneexample/files/music/hello.mp3
//        val path = "/data/user/0/com.example.kotlinringtoneexample/app_hello/hello.mp3"
//            val path = "/data/user/0/com.example.kotlinringtoneexample/app_lonely/lonely.mp3"
//            val path = "/data/user/0/com.example.kotlinringtoneexample/app_aloha/aloha.mp3"
//            val path = result
//        val title = "hello"
//            val title = "lonely"
//            val title = "aloha"

            setRingTone(path, title)
        })

        btn_notification.setOnClickListener(View.OnClickListener {
            setNotificationSound(path, title)

        })

        btn_alarm.setOnClickListener(View.OnClickListener {
            setAlarmManager(path, title)

        })

        btn_ringtone_contact.setOnClickListener(View.OnClickListener {
            setRingTonecontact(path, title)

        })
    }

    fun setAlarmManager(path: String, title: String) {

        val resolver: ContentResolver = context!!.getContentResolver()
//        val file = File(Environment.getExternalStorageDirectory().toString() + "/aloha.mp3")

        val file = File(path)

        if (file.exists()) {

            val oldUri = MediaStore.Audio.Media.getContentUriForPath(file.absolutePath)
            resolver.delete(
                oldUri!!,
                MediaStore.MediaColumns.DATA + "=\"" + file.absolutePath + "\"",
                null
            )

            Log.d(TAG, "path " + file.absolutePath)
            val values = ContentValues()
            values.put(MediaStore.MediaColumns.DATA, file.absolutePath)
            values.put(MediaStore.MediaColumns.TITLE, file.name)
            values.put(MediaStore.MediaColumns.SIZE, file.length())
            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3")
            values.put(MediaStore.Audio.Media.IS_ALARM, true)
            values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
            values.put(MediaStore.Audio.Media.IS_MUSIC, false);


            //Insert it into the database
//            val uri = MediaStore.Audio.Media.getContentUriForPath(file.absolutePath)
            val newUri: Uri?
//            if (uri != null) {
                try {
                    newUri = resolver.insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
//                    newUri = resolver.insert(uri, values);
                    Log.d(TAG, "new uri: " + newUri)

                    RingtoneManager.setActualDefaultRingtoneUri(
                        activity!!.applicationContext,
                        RingtoneManager.TYPE_ALARM,
                        newUri
                    )

                    Settings.System.putString(
                        resolver, Settings.System.ALARM_ALERT,
                        newUri.toString()
                    )

                    // báo thức mặc định
                    val defaultUri = RingtoneManager.getActualDefaultRingtoneUri(
                        requireContext(),
                        RingtoneManager.TYPE_ALARM
                    )
                    Log.d(TAG, "default uri  : " + defaultUri + " path:   " + defaultUri.path + "  " + defaultUri.isAbsolute + " ")


                } catch (e: Exception) {
                    Log.d(TAG, "setRingTone: $e")
                }
//            }
        }
    }


    fun setNotificationSound(path: String, title: String) {
        val resolver: ContentResolver = context!!.getContentResolver()
        val file = File(path)

        if (file.exists()) {
            val oldUri = MediaStore.Audio.Media.getContentUriForPath(file.absolutePath)
            resolver.delete(
                oldUri!!,
                MediaStore.MediaColumns.DATA + "=\"" + file.absolutePath + "\"",
                null
            )

            Log.d(TAG, "setRingTone: path " + file.absolutePath)
            val values = ContentValues()
            values.put(MediaStore.MediaColumns.DATA, file.absolutePath)
            values.put(MediaStore.MediaColumns.TITLE, file.name)
            values.put(MediaStore.MediaColumns.SIZE, 215454)
            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3")
            values.put(MediaStore.Audio.Media.ARTIST, "2ne1")
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true)

            //Insert it into the database
//            val uri = MediaStore.Audio.Media.getContentUriForPath(file.absolutePath)
            val newUri: Uri?
//            if (uri != null) {
//                newUri = resolver.insert(uri, values)
                newUri = resolver.insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
                try {
                    RingtoneManager.setActualDefaultRingtoneUri(
                        context,
                        RingtoneManager.TYPE_NOTIFICATION,
                        newUri
                    )
                } catch (e: Exception) {
                    Log.d(TAG, "setRingTone: $e")
                }
//            }
        }
    }

    fun setRingTone(path: String, title: String) {

        val resolver: ContentResolver = context!!.getContentResolver()

        val file = File(path)
        if (file.exists()) {
            val oldUri = MediaStore.Audio.Media.getContentUriForPath(file.absolutePath)
            resolver.delete(
                oldUri!!,
                MediaStore.MediaColumns.DATA + "=\"" + file.absolutePath + "\"",
                null
            )
            Log.d(TAG, "setRingTone: path " + file.absolutePath)
            val values = ContentValues()
            values.put(MediaStore.MediaColumns.DATA, file.absolutePath)
            values.put(MediaStore.MediaColumns.TITLE, file.name)
            values.put(MediaStore.MediaColumns.SIZE, file.length())
            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3")
            values.put(MediaStore.Audio.Media.ARTIST, "2ne1")
//            values.put(MediaStore.Audio.Media.DURATION, 230)
            values.put(MediaStore.Audio.Media.IS_RINGTONE, true)

            //Insert it into the database
//            val uri = MediaStore.Audio.Media.getContentUriForPath(file.absolutePath)
            val newUri: Uri?
//            if (uri != null) {
                //Insert it into the database
                newUri = resolver.insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
//                newUri = resolver.insert(uri, values)
                try {
                    RingtoneManager.setActualDefaultRingtoneUri(
                        activity!!.applicationContext,
                        RingtoneManager.TYPE_RINGTONE,
                        newUri
                    )

                    Settings.System.putString(
                        resolver, Settings.System.RINGTONE,
                        newUri.toString()
                    )

                } catch (e: Exception) {
                    Log.d(TAG, "setRingTone: $e")
                }
//            }
        }

    }

//    fun setAsRingtone(path: String, title : String) {
//
//        val resolver: ContentResolver = context!!.getContentResolver()
//
//        val file = File(path)
//
//        val values = ContentValues()
//        values.put(MediaStore.MediaColumns.TITLE, file.name)
//        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3")
//        values.put(MediaStore.MediaColumns.SIZE, file.length())
//        values.put(MediaStore.Audio.Media.ARTIST, R.string.app_name)
//        values.put(MediaStore.Audio.Media.IS_RINGTONE, true)
//        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true)
//        values.put(MediaStore.Audio.Media.IS_ALARM, true)
//        values.put(MediaStore.Audio.Media.IS_MUSIC, false)
//        val newUri: Uri =
//            resolver.insert(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, values)!!
//        try {
//            resolver.openOutputStream(newUri).use { os -> }
//        } catch (ignored: java.lang.Exception) {
//        }
//        RingtoneUtils.setRingtone(context!!, newUri, RingtoneManager.TYPE_RINGTONE)
//    }


    fun setRingTonecontact(path: String, title: String) {
        val values = ContentValues()
        val resolver: ContentResolver = context!!.getContentResolver()

        val file = File(path)

        if (file.exists()) {
            val oldUri = MediaStore.Audio.Media.getContentUriForPath(file.absolutePath)
            resolver.delete(
                oldUri!!,
                MediaStore.MediaColumns.DATA + "=\"" + file.absolutePath + "\"",
                null
            )
            val contact_number = "0942132785"
            val lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                contact_number
            )

            // The columns used for `Contacts.getLookupUri`
            val projection =
                arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY)
            val data: Cursor = context!!.getContentResolver()
                .query(lookupUri, projection, null, null, null)!!
            if (data != null && data.moveToFirst()) {
                data.moveToFirst()
                // Get the contact lookup Uri
                val contactId = data.getLong(0)
                val lookupKey = data.getString(1)
                val contactUri = ContactsContract.Contacts.getLookupUri(contactId, lookupKey)
                values.put(MediaStore.MediaColumns.DATA, file.absolutePath)
                values.put(MediaStore.MediaColumns.TITLE, title)
                values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3")
                values.put(MediaStore.Audio.Media.IS_RINGTONE, true)
                val uri = MediaStore.Audio.Media.getContentUriForPath(file.absolutePath)
                val newUri = resolver.insert(uri!!, values)
                if (newUri != null) {
                    val uriString = newUri.toString()
                    values.put(ContactsContract.Contacts.CUSTOM_RINGTONE, uriString)
                    Log.e("Uri String for " + ContactsContract.Contacts.CONTENT_URI, uriString)
                    val updated = resolver.update(contactUri, values, null, null).toLong()
                    Toast.makeText(context, "Updated : $updated", Toast.LENGTH_LONG).show()
                }
                data.close()
            }
        } else {
            Toast.makeText(context, "File does not exist", Toast.LENGTH_LONG).show()
        }
    }


    fun saveas(): String {
        val cw = ContextWrapper(context)
//        val ressound: Int = R.raw.hello
        val ressound: Int = R.raw.lonely
//        val ressound: Int = R.raw.aloha
        var buffer: ByteArray? = null
        val fIn: InputStream = context!!.getResources().openRawResource(ressound)
        var size = 0

        //1st part
        try {
            size = fIn.available()
            buffer = ByteArray(size)
            fIn.read(buffer)
            fIn.close()
        } catch (e: IOException) {
            Log.e(TAG, "IOException first part")
//            return false
        }
//        val soundname = "hello"
        val soundname = "lonely"
//        val soundname = "aloha"
        val filename = "$soundname.mp3"

//        val path =
//            "/storage/emulated/0/Android/data/com.example.kotlinringtoneexample/files/music" + "/$filename"

        val path: String =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .toString()

        val directory = cw.getDir(soundname, Context.MODE_PRIVATE)
        val fullPath = File(path)
//        val fullPath = File(directory, filename)

//        val exists = File(directory).exists()
        if (fullPath.exists()) {
            fullPath.mkdirs()
        }

        //second part
        val save: FileOutputStream
        try {
            save = FileOutputStream(fullPath)
            save.write(buffer)
            save.flush()
            save.close()
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "FileNotFoundException in second part")
//            return false
        } catch (e: IOException) {
            Log.e(TAG, "IOException in second part")
//            return false
        }
        return fullPath.absolutePath
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