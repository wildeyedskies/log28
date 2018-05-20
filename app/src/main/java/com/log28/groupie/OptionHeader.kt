package com.log28.groupie

import com.log28.R
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.option_activity_header_item.*


class OptionHeader(val string: String): Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.option_item_header_name.text = string
    }

    override fun getLayout(): Int {
        return R.layout.option_activity_header_item
    }
}