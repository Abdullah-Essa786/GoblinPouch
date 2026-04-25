package com.example.goblinpouchdemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.goblinpouchdemo.databinding.ItemCategoryRowBinding
import com.example.goblinpouchdemo.models.Category

// CategorySummary holds the data for one row in the category breakdown list

// same Adapter pattern as ExpenseAdapter but for category summary rows
class CategoryAdapter(
    private val categories: MutableList<Category>
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(val binding: ItemCategoryRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Category) {
            binding.tvCategoryName.text = item.name
            binding.tvAmountSpent.text = "R %.2f".format(item.totalSpent)

            if (item.budgetSet > 0){
                binding.progressCategory.visibility = View.VISIBLE
                binding.tvBudgetLimit.visibility = View.VISIBLE
                binding.tvProgressLabel.visibility = View.VISIBLE

                val percentage = ((item.totalSpent / item.budgetSet) * 100).toInt()
                binding.progressCategory.progress = percentage
                binding.tvBudgetLimit.text = "R %.2f".format(item.budgetSet)
                binding.tvProgressLabel.text = "$percentage%"

                if (item.totalSpent > item.budgetSet){
                    binding.tvAmountSpent.setTextColor(binding.root.context.getColor(R.color.red))
                }
                else{
                    binding.tvAmountSpent.setTextColor(binding.root.context.getColor(R.color.white))
                }

            }
            else{
                binding.progressCategory.visibility = View.GONE
                binding.tvBudgetLimit.visibility = View.GONE
                binding.tvProgressLabel.text = "No budget set"
            }
            binding.tvDateRange.visibility = View.GONE

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
    fun submitList(newList: List<Category>) {
        categories.clear()
        categories.addAll(newList)
        notifyDataSetChanged()
    }
}