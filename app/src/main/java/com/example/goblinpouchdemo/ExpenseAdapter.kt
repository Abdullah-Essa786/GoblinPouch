package com.example.goblinpouchdemo

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.goblinpouchdemo.databinding.ItemExpenseCardBinding
import com.example.goblinpouchdemo.models.Expense

// Adapter = the middleman between a list of data and the RecyclerView that displays it
class ExpenseAdapter(
    private val expenses: MutableList<Expense>
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    // optional click handler — set this from the Activity to react to taps
    var onItemClick: ((Expense) -> Unit)? = null

    // ViewHolder = holds references to the views inside one list item card
    inner class ExpenseViewHolder(val binding: ItemExpenseCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // bind = fill this card's views with data from one Expense object
        fun bind(expense: Expense) {
            binding.tvExpenseAmount.text = "R %.2f".format(expense.amount)
            binding.tvExpenseNotes.text = expense.description
            binding.tvExpenseDate.text = expense.date
            binding.tvCategoryBadge.text = expense.category

            // check if this expense has a receipt image attached
            if (expense.attachment != "None" && expense.attachment.isNotEmpty()) {
                try {
                    // the image is stored as a Base64 string in Firebase
                    // decode it back into bytes, then into a Bitmap we can display
                    val bytes = Base64.decode(expense.attachment, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    binding.ivReceiptImage.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    // if decoding fails for any reason, show the placeholder image
                    binding.ivReceiptImage.setImageResource(R.drawable.ic_no_image_placeholder)
                }
            } else {
                // no receipt — show the placeholder image
                binding.ivReceiptImage.setImageResource(R.drawable.ic_no_image_placeholder)
            }

            // when the card is tapped, fire the click handler if one is set
            binding.root.setOnClickListener { onItemClick?.invoke(expense) }
        }
    }

    // called by RecyclerView when it needs a new blank card to fill
    // we inflate (build) a card from item_expense_card.xml and wrap it in a ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ExpenseViewHolder(binding)
    }

    // called by RecyclerView when it's ready to fill a card at a specific position
    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(expenses[position])
    }

    // tells RecyclerView how many cards to create
    override fun getItemCount() = expenses.size

    // replace the whole list with new data and refresh the display
    fun submitList(newList: List<Expense>) {
        expenses.clear()
        expenses.addAll(newList)
        notifyDataSetChanged() // tells RecyclerView to redraw all cards
    }
}