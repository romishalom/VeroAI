package com.rando.factchecker2.floatingService.audio

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.ColorDrawable
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.rando.factchecker2.R
import com.rando.factchecker2.floatingService.FloatingService
import java.io.File


class RecordActivity : AppCompatActivity() {

    private lateinit var mediaProjectionManager: MediaProjectionManager
    //private val root: String = Environment.getExternalStorageDirectory().toString()
    //private val app_folder: String = "$root/AudioCaptures"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFormat(PixelFormat.TRANSLUCENT)
        window.setBackgroundDrawableResource(android.R.color.transparent);
        if (intent!= null && intent.action != null) {
            if (intent.action.equals("Record:start")) {
                Log.d("RecordActivity", "onCreate: Record:start")
                startCapturing()

            } else if (intent.action.equals("Record:stop")) {
                Log.d("RecordActivity", "onCreate: Record:stop")
                stopCapturing()
                super.finish()
            } else if (intent.action.equals("Record:end")) {
                Log.d("RecordActivity", "onCreate: Record:end")
                finish()
            }
        }
    }

    private fun finishAndOpenService() {
        if (isMyServiceRunning()) {
            stopService(Intent(this, FloatingService::class.java))
        }
        finish()
        val intent = Intent(this, FloatingService::class.java)
        startService(intent)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // Pass touch events through to underlying views
        return super.dispatchTouchEvent(ev)
    }

    private fun startCapturing() {
        if (!isRecordAudioPermissionGranted()) {
            requestRecordAudioPermission()
        } else {
            startMediaProjectionRequest()
        }
    }

    private fun stopCapturing() {
        startService(Intent(this, AudioCaptureService::class.java).apply {
            action = AudioCaptureService.ACTION_STOP
        })
    }

    private fun isRecordAudioPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestRecordAudioPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            RECORD_AUDIO_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this,
                    "Permissions granted. Click the button once again.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this, "Permissions denied",
                    Toast.LENGTH_SHORT
                ).show()

            }
        }
    }

    /**
     * Before a capture session can be started, the capturing app must
     * call MediaProjectionManager.createScreenCaptureIntent().
     * This will display a dialog to the user, who must tap "Start now" in order for a
     * capturing session to be started. This will allow both video and audio to be captured.
     */
    private fun startMediaProjectionRequest() {
        // use applicationContext to avoid memory leak on Android 10.
        // see: https://partnerissuetracker.corp.google.com/issues/139732252
        mediaProjectionManager =
            applicationContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(
            mediaProjectionManager.createScreenCaptureIntent(),
            MEDIA_PROJECTION_REQUEST_CODE
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MEDIA_PROJECTION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(
                    this,
                    "Starting...",
                    Toast.LENGTH_SHORT
                ).show()

                val audioCaptureIntent = Intent(this, AudioCaptureService::class.java).apply {
                    action = AudioCaptureService.ACTION_START
                    putExtra(AudioCaptureService.EXTRA_RESULT_DATA, data!!)
                }
                startForegroundService(audioCaptureIntent)

            } else {
                Toast.makeText(
                    this, "Request denied",
                    Toast.LENGTH_SHORT
                ).show()
                finishAndOpenService()
            }
        }
    }

    //AAC
    private fun getOutputFile(): File {
        val file = File(getExternalFilesDir(null), "/AudioCaptures/outputFile.aac")
        return file
    }

    private fun getAudioPath(name: String): String {
        val path = File(getExternalFilesDir(null), "/AudioCaptures/$name").absolutePath
        return path
    }

    private fun isMyServiceRunning(): Boolean {
        // The ACTIVITY_SERVICE is needed to retrieve a
        // ActivityManager for interacting with the global system
        // It has a constant String value "activity".

        // A loop is needed to get Service information that
        // are currently running in the System.
        // So ActivityManager.RunningServiceInfo is used.
        // It helps to retrieve a
        // particular service information, here its this service.
        // getRunningServices() method returns a list of the
        // services that are currently running
        // and MAX_VALUE is 2147483647. So at most this many services
        // can be returned by this method.
        @Suppress("DEPRECATION")
        for (service in (baseContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager).getRunningServices(
            Int.MAX_VALUE)) {
            // If this service is found as a running,
            // it will return true or else false.
            if (FloatingService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }


    companion object {
        private const val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 42
        private const val MEDIA_PROJECTION_REQUEST_CODE = 13
    }
}