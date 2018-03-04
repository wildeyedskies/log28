package com.log28.groupie

import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.cycle_overview_item.*
import com.log28.R

class OverviewItem(val string: String): Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.cycle_overview__item.text = string
    }

    override fun getLayout(): Int {
        return R.layout.cycle_overview_item
    }
}