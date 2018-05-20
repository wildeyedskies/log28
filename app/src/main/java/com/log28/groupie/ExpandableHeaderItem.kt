package com.log28.groupie

import android.view.View
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.ExpandableItem
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.day_view_group_item.*
import com.log28.R

class ExpandableHeaderItem(private val categoryText: String) : ExpandableItem, Item() {
    override fun getLayout(): Int {
        return R.layout.day_view_group_item
    }

    private lateinit var expandableGroup: ExpandableGroup

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.categoryText.text = categoryText

        // make the whole darn thingy clickable
        viewHolder.group_view.setOnClickListener {
            expandableGroup.onToggleExpanded()
            bindIcon(viewHolder)
        }

        // Initial icon state -- not animated.
        viewHolder.icon.apply {
            visibility = View.VISIBLE
            setImageResource(if (expandableGroup.isExpanded) R.drawable.chevron_up else R.drawable.chevron_down)
        }
    }

    private fun bindIcon(viewHolder: ViewHolder) {
        viewHolder.icon.apply {
            visibility = View.VISIBLE
            setImageResource(if (expandableGroup.isExpanded) R.drawable.chevron_up else R.drawable.chevron_down)
            //(drawable as Animatable).start()
        }
    }

    override fun setExpandableGroup(onToggleListener: ExpandableGroup) {
        this.expandableGroup = onToggleListener
    }
}