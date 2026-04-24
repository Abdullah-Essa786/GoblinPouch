package com.example.goblinpouchdemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.goblinpouchdemo.databinding.ItemCategoryRowBinding

// CategorySummary holds the data for one row in the category breakdown list
data class CategorySummary(
    val name: String,    // e.g. "Food"
    val total: Double,   // total amount spent in this category
    val count: Int       // number of transactions in this category
)

// same Adapter pattern as ExpenseAdapter but for category summary rows
class CategoryAdapter(
    private val categories: MutableList<CategorySummary>
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(val binding: ItemCategoryRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CategorySummary) {
            binding.tvCategoryName.text = item.name
            binding.tvAmountSpent.text = "R %.2f".format(item.total)

            // show "1 transaction" or "3 transactions" depending on count
            binding.tvProgressLabel.text =
                "${item.count} transaction${if (item.count != 1) "s" else ""}"

            // these views exist in the layout for other screens
            // but aren't needed here so we hide them
            binding.tvBudgetLimit.visibility = View.GONE
            binding.tvDateRange.visibility = View.GONE
            binding.progressCategory.visibility = View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount() = categories.size

    // clear the old list, add the new one, and tell RecyclerView to redraw
    fun submitList(newList: List<CategorySummary>) {
        categories.clear()
        categories.addAll(newList)
        notifyDataSetChanged()
    }
}