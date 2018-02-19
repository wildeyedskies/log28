package org.mcxa.log28.org.mcxa.log28.expandable

import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.day_view_list_item.*
import org.mcxa.log28.R
import org.mcxa.log28.Symptom


class ChildItem(val symptom: Symptom, var state: Boolean, var onClick: () -> Unit): Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.day_item.text = symptom.name
        viewHolder.item_checkbox.isChecked = state

        viewHolder.list_item.setOnClickListener {
            viewHolder.item_checkbox.toggle()
            onClick.invoke()
        }
    }

    fun setChecked() {

    }

    override fun getLayout(): Int {
        return R.layout.day_view_list_item
    }
}