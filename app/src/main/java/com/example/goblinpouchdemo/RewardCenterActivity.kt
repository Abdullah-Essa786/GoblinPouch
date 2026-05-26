package com.example.goblinpouchdemo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Toast
import com.example.goblinpouchdemo.databinding.ActivityRewardCenterBinding
import com.google.firebase.database.FirebaseDatabase

class RewardCenterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRewardCenterBinding

    private val userId = "Abdullah"

    private var currentPoints = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRewardCenterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadRewardData()

        binding.btnBuyGoldenGoblin.setOnClickListener {
            purchaseAvatar(
                avatarId = "golden_goblin",
                cost = 300
            )
        }
        binding.btnBuyKingGoblin.setOnClickListener {
            purchaseAvatar(
                avatarId = "king_goblin",
                cost = 700
            )
        }
    }
    private fun loadRewardData() {

        val rewardRef = FirebaseDatabase.getInstance()
            .getReference("temp/$userId/rewards/points")

        rewardRef.get().addOnSuccessListener {

            currentPoints = it.getValue(Int::class.java) ?: 0

            binding.tvPoints.text = "Goblin Gold: $currentPoints"
        }
    }
    private fun purchaseAvatar(
        avatarId: String,
        cost: Int
    ) {

        if (currentPoints < cost) {
            Toast.makeText(
                this,
                "Not enough Goblin Gold",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        currentPoints -= cost

        val rewardRef = FirebaseDatabase.getInstance()
            .getReference("temp/$userId/rewards")

        rewardRef.child("points")
            .setValue(currentPoints)

        rewardRef.child("unlockedAvatars")
            .child(avatarId)
            .setValue(true)

        binding.tvPoints.text = "Goblin Gold: $currentPoints"

        Toast.makeText(this,
            "Avatar unlocked!",
            Toast.LENGTH_SHORT).show()
    }
}