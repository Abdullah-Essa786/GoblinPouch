package com.example.goblinpouchdemo

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.goblinpouchdemo.databinding.ActivityMainBinding
import com.example.goblinpouchdemo.databinding.ActivityViewReceiptBinding
import com.example.goblinpouchdemo.models.Expense
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlin.math.exp

class ViewReceipt : AppCompatActivity() {

    private lateinit var binding: ActivityViewReceiptBinding
    private val currentUserId = "Abdullah"
    private lateinit var databaseReference : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityViewReceiptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val expenseId = intent.getStringExtra("EXPENSE_ID")

        if (expenseId != null){
            databaseReference = FirebaseDatabase.getInstance().getReference("temp/${currentUserId}/Expense/${expenseId}")
            loadLatestImage()
        }
        else{
            Toast.makeText(this, "Expense ID not found", Toast.LENGTH_LONG).show()
        }

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
        databaseReference.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()){
                val expense = snapshot.getValue(Expense::class.java)
                val imageData = expense?.attachment ?: ""

                if(imageData.isNotEmpty() && imageData != "None"){
                    val bitmap = decodeBase64(imageData)

                    val matrix = Matrix()
                    matrix.postRotate(90f)
                    val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                    binding.ReceiptImage.setImageBitmap(rotatedBitmap)
                }
            }
            else{
                Toast.makeText(this, "No image found", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_LONG).show()
        }
    }

}