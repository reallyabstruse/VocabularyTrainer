package com.reallyabstruse.vocabularytrainer

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner

// Make a dialog box. Call callback if OK is pressed. Pass values from inputFields to callback
fun dialogBox(context: Context, title: String, vararg inputFields: View, callback: (it: List<String>) -> Unit) {
    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
    builder.setTitle(title)

    val layout = LinearLayout(context)
    layout.orientation = LinearLayout.VERTICAL

    inputFields.forEach {
        layout.addView(it)
    }
    builder.setView(layout)

    builder.setPositiveButton(context.getString(R.string.ok), DialogInterface.OnClickListener { _, _ ->
        callback(inputFields.mapNotNull {
            when(it) {
                is EditText -> it.text.toString()
                is ActivityAbstract.LanguagePicker -> it.getValue()
                else -> null
            }?.also { str ->
                if (str.isEmpty()) {
                    return@OnClickListener
                }
            }
        })

    })
    builder.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }

    builder.show()
}