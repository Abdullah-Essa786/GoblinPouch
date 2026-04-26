package com.example.goblinpouchdemo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraExecutor
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LifecycleOwner
import com.example.goblinpouchdemo.databinding.ActivityReceiptCaptureBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ReceiptCapture : AppCompatActivity() {

    private lateinit var binding : ActivityReceiptCaptureBinding
    private lateinit var databaseReference : DatabaseReference
    private lateinit var imageCapture: ImageCapture
    private lateinit var outPutDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var currentUserID: String
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()
        currentUserID = auth.currentUser?.uid ?: ""

        binding = ActivityReceiptCaptureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseReference = FirebaseDatabase.getInstance().getReference("Users/$currentUserID/expenses")

        if (allPermissionsGranted()){
            startCamera()
        }
        else{
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA), 10)
        }

        outPutDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
        binding.captureButton.setOnClickListener {
            takePhoto()
        }

    }

    private fun takePhoto(){
        val imageCapture = imageCapture
        val  photoFile = File(outPutDirectory, SimpleDateFormat("yyyyMMdd-HHmmss",
            Locale.US).format(System.currentTimeMillis()) + ".jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback{
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)

                    //converting image to base64 so it can save on firebase
                    proceedToReview(bitmap)
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@ReceiptCapture,
                        "Not Saved", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let{
            File(it, "simple camera").apply { mkdirs() }
        }
        return mediaDir?:filesDir
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this as LifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            preview.setSurfaceProvider(binding.previewView.surfaceProvider)
        }, ContextCompat.getMainExecutor(this))
    }
    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(this,
        android.Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        if(requestCode==10 &&allPermissionsGranted()){
            startCamera()
        }
        else{
            Toast.makeText(this, "Camera permissions required",
                Toast.LENGTH_LONG).show()
        }
    }

    private fun proceedToReview(bitmap: Bitmap){
        val b = ByteArrayOutputStream()
        val expenseId = intent.getStringExtra("EXPENSE_ID") ?: return

        val maxSize = 1024
        val width = bitmap.width
        val height = bitmap.height
        val ratio = width.toFloat() / height.toFloat()

        val finalWidth: Int
        val finalHeight: Int
        if (width > height) {
            finalWidth = maxSize
            finalHeight = (maxSize / ratio).toInt()
        } else {
            finalHeight = maxSize
            finalWidth = (maxSize * ratio).toInt()
        }

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)

        // REDUCE QUALITY to 20 or 30.
        // This makes the string small enough for the Realtime Database.
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, b)

        val imageBytes = b.toByteArray()
        val base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT)

        val intent = Intent(this, ViewReceipt::class.java).apply {
            putExtra("EXPENSE_ID", expenseId)
            putExtra("IMAGE_DATA", base64Image)
            putExtra("IS_PREVIEW", true)
        }
        startActivity(intent)
        finish()
    }

//    private fun saveImageToFirebase(bitmap: Bitmap){
//        val b = ByteArrayOutputStream()
//        val expenseId = intent.getStringExtra("EXPENSE_ID") ?: return
//
//        // REDUCE QUALITY to 20 or 30.
//        // This makes the string small enough for the Realtime Database.
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, b)
//
//        val imageBytes = b.toByteArray()
//        val base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT)
//
//        // Check if the string is too big (Realtime Database nodes should stay under 10MB,
//        // but ideally under 1MB for performance)
//        if (base64Image.length > 1000000) {
//            Toast.makeText(this, "Image too large, try a closer crop", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        databaseReference.child(expenseId).child("attachment").setValue(base64Image)
//            .addOnSuccessListener {
//                Toast.makeText(this, "Receipt Saved to DB!", Toast.LENGTH_LONG).show()
//                val intent = Intent(this, ViewReceipt::class.java)
//                intent.putExtra("EXPENSE_ID", expenseId)
//                startActivity(intent)
//                finish()
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(this, "Failed to save receipt: ${e.message}", Toast.LENGTH_LONG).show()
//            }
//    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

}