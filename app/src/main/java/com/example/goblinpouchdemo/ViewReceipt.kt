package com.example.goblinpouchdemo

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.goblinpouchdemo.databinding.ActivityMainBinding
import com.example.goblinpouchdemo.databinding.ActivityViewReceiptBinding
import com.example.goblinpouchdemo.models.Expense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlin.math.exp

class ViewReceipt : AppCompatActivity() {

    private lateinit var binding: ActivityViewReceiptBinding
    private lateinit var currentUserId: String
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference : DatabaseReference
    private var base64Image: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid ?: ""

        databaseReference = FirebaseDatabase.getInstance().getReference("Users/$currentUserId/expenses")

        binding = ActivityViewReceiptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isPreview = intent.getBooleanExtra("IS_PREVIEW", false)
        val expenseId = intent.getStringExtra("EXPENSE_ID") ?: ""
        base64Image = intent.getStringExtra("IMAGE_DATA")

        if (isPreview){
            binding.PreviewButtons.visibility = View.VISIBLE
            binding.HomeButton.visibility = View.GONE

            base64Image?.let {
                binding.ReceiptImage.setImageBitmap(decodeAndRotate(it))
            }
        }
        else{
            binding.PreviewButtons.visibility = View.GONE
            binding.HomeButton.visibility = View.VISIBLE
            loadLatestImage(expenseId)
        }

        binding.btnRetake.setOnClickListener {
            val intent = Intent(this, ReceiptCapture::class.java)
            intent.putExtra("EXPENSE_ID", expenseId)
            startActivity(intent)
            finish()
        }

        binding.btnSave.setOnClickListener {
            saveToFirebase(expenseId ?: "", base64Image)
        }
        binding.HomeButton.setOnClickListener {
            finish()
        }

    }

    private fun decodeAndRotate(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

        // Rotate 90 degrees for portrait camera shots
        val matrix = Matrix()
        matrix.postRotate(90f)

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun loadLatestImage(expenseId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Users/$currentUserId/expenses/${expenseId}")

        ref.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()){
                val expense = snapshot.getValue(Expense::class.java)
                val imageData = expense?.attachment ?: ""

                if(imageData.isNotEmpty() && imageData != "None"){
                    binding.ReceiptImage.setImageBitmap(decodeAndRotate(imageData))
                }
            }
            else{
                Toast.makeText(this, "No image found", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveToFirebase(expenseId: String, data: String?){
        if (data == null || expenseId.isEmpty()) return

        databaseReference.child(expenseId).child("attachment").setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Receipt Saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

}