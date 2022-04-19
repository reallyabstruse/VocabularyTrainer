package com.reallyabstruse.vocabularytrainer

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow

class GridMaker(private val context: Context, private val table: TableLayout, private val width: Int, private val rowHeight: Int = TableRow.LayoutParams.MATCH_PARENT) {
    init {
        table.removeAllViews()
    }

    private var curRow: TableRow = TableRow(context)
    private var itemsInRow: Int = 0

    fun addItem(item: View, stretch: Boolean = true) {
        item.layoutParams = TableRow.LayoutParams(
            if(stretch) 0 else TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT,
            if(stretch) 1.0f else 0f
        )
        item.foregroundGravity = Gravity.CENTER
        curRow.addView(item)
        if (++itemsInRow >= width) {
            addRow()
            curRow = TableRow(context)
        }
    }

    fun finalize() {
        while (itemsInRow != 0) {
            addItem(LinearLayout(context))
        }
    }

    private fun addRow() {
        curRow.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            rowHeight
        )
        table.addView(curRow)
        itemsInRow = 0
    }
}