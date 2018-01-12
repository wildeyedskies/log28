package org.mcxa.log28

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.day_view_list_item.view.*
import java.util.*

// contains the logic for the ExpandableList used in the day view
class DayExpandableListAdapter(private val activity: FragmentActivity?,
                               private val day: Calendar) :
        BaseExpandableListAdapter() {

    private var dayData = AppDatabase.getDataByDate(day) ?: DayData(day.formatDate())

    override fun getGroup(p0: Int): Any {
        return DayData.categories[p0]
    }

    override fun isChildSelectable(p0: Int, p1: Int): Boolean {
        return true
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    fun toggleStatus(catIndex: Int, itemIndex: Int) {
        Log.d("EXPAND_LIST", "Toggle status called")
        dayData.update(catIndex, itemIndex)
        notifyDataSetChanged()
    }

    fun changeDay(newDay: Calendar) {
        dayData = AppDatabase.getDataByDate(newDay) ?: DayData(newDay.formatDate())
        notifyDataSetChanged()
    }

    override fun getGroupView(catIndex: Int, isExpanded: Boolean,
                              convertView: View?, parent: ViewGroup?): View {
        val view = if (convertView != null) convertView else {
            val layoutInflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            layoutInflater.inflate(R.layout.day_view_group_item, null)
        }
        view.findViewById<TextView>(R.id.categoryText).text = DayData.categories[catIndex]
        return view
    }

    override fun getChildrenCount(catIndex: Int): Int {
        return DayData.items[catIndex].size
    }

    override fun getChild(catIndex: Int, itemIndex: Int): Any {
        return DayData.items[catIndex][itemIndex]
    }

    override fun getGroupId(groupIndex: Int): Long {
        return groupIndex.toLong()
    }

    override fun getChildView(catIndex: Int, itemIndex: Int,
                              isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val view = if (convertView != null) convertView else {
            val layoutInflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            layoutInflater.inflate(R.layout.day_view_list_item, null)
        }
        view.day_item.text = DayData.items[catIndex][itemIndex]
        view.item_checkbox.isChecked = dayData.getItemState(catIndex, itemIndex)
        return view
    }

    override fun getChildId(catIndex: Int, itemIndex: Int): Long {
        return itemIndex.toLong()
    }

    override fun getGroupCount(): Int {
        return DayData.categories.size
    }
}