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


/*   val file = context!!.getExternalFilesDir("music") as File;
   val path = file.absolutePath + File.separator + "lonely.mp3"*/

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


//        val file = File(Environment.getExternalStorageDirectory().toString() + "/Music/lonely.mp3")
        val file = File(Environment.getExternalStorageDirectory().toString() + "/Download/lonely.mp3") // dùng cho máy ảo
        val path = file.absolutePath

        Log.d(TAG, "onViewCreated: path default : " + path)
        if (file.exists()){
            Log.d(TAG, "onViewCreated: exists : "+ file.absoluteFile)
        }

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
            setRingToneWithContact(path)
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
            var oldUri = getUriFromFile(filePath)
            if (oldUri != null) {
                return oldUri
            } else {
                val values = ContentValues()
                values.put(MediaStore.Audio.AudioColumns.DATA, file.absolutePath)
                values.put(MediaStore.Audio.AudioColumns.TITLE, file.name)
                values.put(MediaStore.Audio.AudioColumns.SIZE, file.length())
                values.put(MediaStore.Audio.AudioColumns.MIME_TYPE, "audio/mp3")
                values.put(MediaStore.Audio.AudioColumns.IS_ALARM, true)
                when (typeRing) {
                    IS_ALARM -> values.put(MediaStore.Audio.AudioColumns.IS_ALARM, true)
                    IS_NOTIFICATION -> values.put(
                        MediaStore.Audio.AudioColumns.IS_NOTIFICATION,
                        true
                    )
                    IS_RINGTONE -> values.put(MediaStore.Audio.AudioColumns.IS_RINGTONE, true)
                }

                val audioCollection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                return resolver.insert(audioCollection, values)

            }

//            val uri = MediaStore.Audio.Media.getContentUriForPath(file.absolutePath)
//            if (uri != null) {
//                    newUri = resolver.insert(uri, values);
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
            Toast.makeText(context, "Fail", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(context, "Fail", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(context, "Fail", Toast.LENGTH_SHORT).show()
        }
    }

    fun setRingToneWithContact(filePath: String) {
        val values = ContentValues()
        val resolver: ContentResolver = context!!.getContentResolver()
        val uri = getOrNew(filePath, IS_RINGTONE)
        if (uri != null) {

            val contact_number = "0942132785"
            val lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                contact_number
            )
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

                val uriString = uri.toString()
                values.put(ContactsContract.Contacts.CUSTOM_RINGTONE, uriString)

                Log.e("Uri String for " + ContactsContract.Contacts.CONTENT_URI, uriString)
                resolver.update(contactUri, values, null, null).toLong()


                data.close()
            }
        } else {
            Toast.makeText(context, "Fail", Toast.LENGTH_SHORT).show()
        }
    }


//    fun setRingTonecontact(path: String) {
//        val values = ContentValues()
//        val resolver: ContentResolver = context!!.getContentResolver()
//
//        val file = File(path)
//
//        if (file.exists()) {
////            val oldUri = MediaStore.Audio.Media.getContentUriForPath(file.absolutePath)
////            resolver.delete(
////                oldUri!!,
////                MediaStore.MediaColumns.DATA + "=\"" + file.absolutePath + "\"",
////                null
////            )
//            val contact_number = "0942132785"
//            val lookupUri = Uri.withAppendedPath(
//                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
//                contact_number
//            )
//            // The columns used for `Contacts.getLookupUri`
//            val projection =
//                arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY)
//            val data: Cursor = context!!.getContentResolver()
//                .query(lookupUri, projection, null, null, null)!!
//            if (data != null && data.moveToFirst()) {
//                data.moveToFirst()
//                // Get the contact lookup Uri
//                val contactId = data.getLong(0)
//                val lookupKey = data.getString(1)
//                val contactUri = ContactsContract.Contacts.getLookupUri(contactId, lookupKey)
//
//                values.put(MediaStore.MediaColumns.DATA, file.absolutePath)
//                values.put(MediaStore.MediaColumns.TITLE, file.name)
//                values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3")
//                values.put(MediaStore.Audio.Media.IS_RINGTONE, true)
//
//                val uri = MediaStore.Audio.Media.getContentUriForPath(file.absolutePath)
//                val newUri = resolver.insert(uri!!, values)
//                if (newUri != null) {
//                    val uriString = newUri.toString()
//                    values.put(ContactsContract.Contacts.CUSTOM_RINGTONE, uriString)
//                    Log.e("Uri String for " + ContactsContract.Contacts.CONTENT_URI, uriString)
//                    val updated = resolver.update(contactUri, values, null, null).toLong()
//                    Toast.makeText(context, "Updated : $updated", Toast.LENGTH_LONG).show()
//                }
//
////                val oldUri = MediaStore.Audio.Media.getContentUriForPath(file.absolutePath)
////                if (checkExits(oldUri.toString())) {
////                    val uriString = oldUri.toString()
////                    values.put(ContactsContract.Contacts.CUSTOM_RINGTONE, uriString)
////                    Log.e("Uri String for " + ContactsContract.Contacts.CONTENT_URI, uriString)
////                    val updated = resolver.update(contactUri, values, null, null).toLong()
////                    Toast.makeText(context, "Updated : $updated", Toast.LENGTH_LONG).show()
////                } else {
////
////                }
////                    val uri = MediaStore.Audio.Media.getContentUriForPath(file.absolutePath)
////                    val newUri = resolver.insert(uri!!, values)
////                    if (newUri != null) {
////                        val uriString = newUri.toString()
////                        values.put(ContactsContract.Contacts.CUSTOM_RINGTONE, uriString)
////                        Log.e("Uri String for " + ContactsContract.Contacts.CONTENT_URI, uriString)
////                        val updated = resolver.update(contactUri, values, null, null).toLong()
////                        Toast.makeText(context, "Updated : $updated", Toast.LENGTH_LONG).show()
////                    }
////                }
//                data.close()
//            }
//
//
//        } else {
//            Toast.makeText(context, "File does not exist", Toast.LENGTH_LONG).show()
//        }
//    }


//    fun saveas(): String {
//        val cw = ContextWrapper(context)
////        val ressound: Int = R.raw.hello
//        val ressound: Int = R.raw.lonely
////        val ressound: Int = R.raw.aloha
//        var buffer: ByteArray? = null
//        val fIn: InputStream = context!!.getResources().openRawResource(ressound)
//        var size = 0
//
//        //1st part
//        try {
//            size = fIn.available()
//            buffer = ByteArray(size)
//            fIn.read(buffer)
//            fIn.close()
//        } catch (e: IOException) {
//            Log.e(TAG, "IOException first part")
////            return false
//        }
////        val soundname = "hello"
//        val soundname = "lonely"
////        val soundname = "aloha"
//        val filename = "$soundname.mp3"
//
////        val path =
////            "/storage/emulated/0/Android/data/com.example.kotlinringtoneexample/files/music" + "/$filename"
//
//        val path: String =
//            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//                .toString()
//
//        val directory = cw.getDir(soundname, Context.MODE_PRIVATE)
//        val fullPath = File(path)
////        val fullPath = File(directory, filename)
//
////        val exists = File(directory).exists()
//        if (fullPath.exists()) {
//            fullPath.mkdirs()
//        }
//
//        //second part
//        val save: FileOutputStream
//        try {
//            save = FileOutputStream(fullPath)
//            save.write(buffer)
//            save.flush()
//            save.close()
//        } catch (e: FileNotFoundException) {
//            Log.e(TAG, "FileNotFoundException in second part")
////            return false
//        } catch (e: IOException) {
//            Log.e(TAG, "IOException in second part")
////            return false
//        }
//        return fullPath.absolutePath
//    }

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