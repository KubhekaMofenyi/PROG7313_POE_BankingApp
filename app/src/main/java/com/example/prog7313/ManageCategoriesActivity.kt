package com.example.prog7313

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class ManageCategoriesActivity : AppCompatActivity() {

    private lateinit var categoryDao: CategoryDao
    private lateinit var expenseDao: ExpenseDao
    private lateinit var categoryLimitDao: CategoryLimitDao
    private lateinit var adapter: CategoryAdapter
    private val categories = mutableListOf<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_categories)

        val db = AppDatabase.getDatabase(this)
        categoryDao = db.categoryDao()
        expenseDao = db.expenseDao()
        categoryLimitDao = db.categoryLimitDao()

        val rvCategories = findViewById<RecyclerView>(R.id.rvCategories)
        rvCategories.layoutManager = LinearLayoutManager(this)
        adapter = CategoryAdapter(categories,
            onEdit = { category -> showEditDialog(category) },
            onDelete = { category -> confirmDelete(category) }
        )
        rvCategories.adapter = adapter

        findViewById<Button>(R.id.btnAddCategory).setOnClickListener {
            showAddCategoryDialog()
        }

        loadCategories()
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            val list = categoryDao.getAllCategories()
            categories.clear()
            categories.addAll(list)
            adapter.notifyDataSetChanged()
        }
    }

    private fun showAddCategoryDialog() {
        val input = EditText(this)
        input.hint = "Category name"
        var selectedColor = "#C77921"

        AlertDialog.Builder(this)
            .setTitle("New Category")
            .setView(input)
            .setNeutralButton("Choose Colour") { _, _ ->
                showColorPicker(selectedColor) { color ->
                    selectedColor = color
                    Toast.makeText(this, "Colour selected", Toast.LENGTH_SHORT).show()
                }
            }
            .setPositiveButton("Add") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    lifecycleScope.launch {
                        if (categoryDao.countByNameIgnoreCase(name) > 0) {
                            Toast.makeText(this@ManageCategoriesActivity, "Category already exists", Toast.LENGTH_SHORT).show()
                        } else {
                            categoryDao.insertCategory(Category(name = name, color = selectedColor))
                            loadCategories()
                            Toast.makeText(this@ManageCategoriesActivity, "Category added", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditDialog(category: Category) {
        var currentName = category.name
        var currentColor = category.color

        fun showEditDialogInternal() {
            val input = EditText(this)
            input.setText(currentName)
            input.hint = "New name"

            AlertDialog.Builder(this)
                .setTitle("Edit Category")
                .setView(input)
                .setNeutralButton("Change Colour") { _, _ ->
                    showColorPicker(currentColor) { selectedColor ->
                        currentColor = selectedColor
                        Toast.makeText(this, "Colour updated. Reopen edit to save changes.", Toast.LENGTH_SHORT).show()
                        showEditDialogInternal() // Re-open edit dialog
                    }
                }
                .setPositiveButton("Save") { _, _ ->
                    val newName = input.text.toString().trim()
                    lifecycleScope.launch {
                        if (newName.isNotEmpty() && newName != currentName) {
                            if (categoryDao.countByNameIgnoreCase(newName) > 0) {
                                Toast.makeText(this@ManageCategoriesActivity, "Name already used", Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            currentName = newName
                        }
                        val updated = category.copy(name = currentName, color = currentColor)
                        categoryDao.updateCategory(updated)
                        loadCategories()
                        Toast.makeText(this@ManageCategoriesActivity, "Category updated", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        showEditDialogInternal()
    }

    private fun showColorPicker(currentColor: String, onColorSelected: (String) -> Unit) {
        val colors = listOf(
            "#E57373", "#FFB74D", "#FFF176", "#81C784",
            "#64B5F6", "#BA68C8", "#F06292", "#A1887F"
        )
        val colorNames = listOf("Red", "Orange", "Yellow", "Green", "Blue", "Purple", "Pink", "Brown")

        AlertDialog.Builder(this)
            .setTitle("Select Colour")
            .setItems(colorNames.toTypedArray()) { _, which ->
                onColorSelected(colors[which])
            }
            .show()
    }

    private fun confirmDelete(category: Category) {
        AlertDialog.Builder(this)
            .setTitle("Delete Category")
            .setMessage("All expenses in '${category.name}' will be moved to 'Uncategorised'. Continue?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    // Ensure "Uncategorised" exists
                    var uncat = categoryDao.getCategoryByName("Uncategorised")
                    if (uncat == null) {
                        categoryDao.insertCategory(Category(name = "Uncategorised", color = "#9E9E9E"))
                    }
                    // Reassign expenses
                    expenseDao.reassignCategory(category.name, "Uncategorised")
                    // Delete the category limit
                    categoryLimitDao.deleteLimitForCategory(category.name)
                    // Delete the category
                    categoryDao.deleteCategory(category)
                    loadCategories()
                    Toast.makeText(this@ManageCategoriesActivity, "Category deleted", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    inner class CategoryAdapter(
        private val items: List<Category>,
        private val onEdit: (Category) -> Unit,
        private val onDelete: (Category) -> Unit
    ) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_manage, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val cat = items[position]
            holder.tvName.text = cat.name
            try {
                holder.viewColorIndicator.setBackgroundColor(Color.parseColor(cat.color))
            } catch (e: Exception) {
                holder.viewColorIndicator.setBackgroundColor(Color.parseColor("#C77921"))
            }
            holder.btnEdit.setOnClickListener { onEdit(cat) }
            holder.btnDelete.setOnClickListener { onDelete(cat) }
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val viewColorIndicator: View = itemView.findViewById(R.id.viewColorIndicator)
            val tvName: TextView = itemView.findViewById(R.id.tvCategoryName)
            val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
            val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
        }
    }
}