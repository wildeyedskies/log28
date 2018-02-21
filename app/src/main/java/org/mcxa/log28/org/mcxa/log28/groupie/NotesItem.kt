package org.mcxa.log28.org.mcxa.log28.groupie

import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.day_view_notes_item.*
import org.mcxa.log28.DayData
import org.mcxa.log28.R


class NotesItem(var daydata: DayData): Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.notes.setText(daydata.notes, TextView.BufferType.EDITABLE)
        viewHolder.notes.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(text: Editable?) {
                daydata.updateNotes(text.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //we don't care
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //we don't care
            }

        })
    }

    override fun getLayout(): Int = R.layout.day_view_notes_item

}