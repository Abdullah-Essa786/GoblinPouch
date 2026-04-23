package com.example.goblinpouchdemo

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.goblinpouchdemo.databinding.ActivityMainBinding
import com.example.goblinpouchdemo.databinding.ActivityViewReceiptBinding
import com.google.firebase.database.FirebaseDatabase

class ViewReceipt : AppCompatActivity() {

    private lateinit var binding: ActivityViewReceiptBinding
    private val databaseReference = FirebaseDatabase.getInstance().getReference("temp/Abdullah/Images")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityViewReceiptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadLatestImage()
        binding.HomeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }

    private fun decodeBase64(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    private fun loadLatestImage() {
        databaseReference.limitToLast(1).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()){
                for (child in snapshot.children){
                    val imageData = child.child("image").value.toString() ?: ""
                    val bitmap = decodeBase64(imageData)

                    val matrix = android.graphics.Matrix()
                    matrix.postRotate(90f)
                    val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                    binding.ReceiptImage.setImageBitmap(rotatedBitmap)
                }
            }
        }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_LONG).show()
            }
    }

}