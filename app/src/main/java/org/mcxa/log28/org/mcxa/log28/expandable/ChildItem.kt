package org.mcxa.log28.org.mcxa.log28.expandable

import android.view.View
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.day_view_list_item.*
import org.mcxa.log28.R


class ChildItem(private val symptomText: String, private val initalState: Boolean, private val onClick: () -> Unit): Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.day_item.text = symptomText
        viewHolder.item_checkbox.isChecked = initalState

        viewHolder.list_item.setOnClickListener {
            viewHolder.item_checkbox.toggle()
            onClick.invoke()
        }
    }


    override fun getLayout(): Int {
        return R.layout.day_view_list_item
    }
}