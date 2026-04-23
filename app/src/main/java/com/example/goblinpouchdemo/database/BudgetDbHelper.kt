package com.example.goblinpouchdemo.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BudgetDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createExpensesTable = """
            CREATE TABLE expenses (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                description TEXT NOT NULL,
                amount REAL NOT NULL,
                date TEXT NOT NULL,
                category TEXT NOT NULL,
                attachment TEXT
            )
        """.trimIndent()

        db.execSQL(createExpensesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS expenses")
        onCreate(db)
    }

    fun insertExpense(
        name: String,
        description: String,
        amount: Double,
        date: String,
        category: String,
        attachment: String = ""
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues()

        values.put("name", name)
        values.put("description", description)
        values.put("amount", amount)
        values.put("date", date)
        values.put("category", category)
        values.put("attachment", attachment)

        val result = db.insert("expenses", null, values)
        db.close()

        return result != -1L
    }

    fun getCategoryTotals(startDate: String, endDate: String): List<CategoryTotalItem> {
        val totals = mutableListOf<CategoryTotalItem>()
        val db = readableDatabase

        val query = """
            SELECT category, SUM(amount) AS totalAmount
            FROM expenses
            WHERE date BETWEEN ? AND ?
            GROUP BY category
            ORDER BY totalAmount DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(startDate, endDate))

        if (cursor.moveToFirst()) {
            do {
                val category = cursor.getString(cursor.getColumnIndexOrThrow("category"))
                val totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("totalAmount"))
                totals.add(CategoryTotalItem(category, totalAmount))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return totals
    }

    companion object {
        private const val DATABASE_NAME = "goblin_pouch.db"
        private const val DATABASE_VERSION = 1
    }
}

data class CategoryTotalItem(
    val category: String,
    val totalAmount: Double
)