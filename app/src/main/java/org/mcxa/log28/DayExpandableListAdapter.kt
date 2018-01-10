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

data class Symptom(val name: String, var status: Boolean)

// contains the logic for the ExpandableList used in the day view
class DayExpandableListAdapter(private val activity: FragmentActivity?,
                               private val day: Calendar) :
        BaseExpandableListAdapter() {

    private var dayData = AppDatabase.getDataByDate(day) ?: DayData(day.formatDate())
    private var items = dayData.buildSymptomMap()
    private var categories = items.keys.toList()

    override fun getGroup(p0: Int): Any {
        return categories[p0]
    }

    override fun isChildSelectable(p0: Int, p1: Int): Boolean {
        return true
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    fun toggleStatus(catIndex: Int, itemIndex: Int) {
        Log.d("EXPAND_LIST", "Toggle status called")
        dayData.updateSymptom(catIndex, itemIndex)
        items = dayData.buildSymptomMap()
        notifyDataSetChanged()
    }

    fun changeDay(newDay: Calendar) {
        dayData = AppDatabase.getDataByDate(newDay) ?: DayData(newDay.formatDate())
        items = dayData.buildSymptomMap()
        categories = items.keys.toList()
        notifyDataSetChanged()
    }

    override fun getGroupView(catIndex: Int, isExpanded: Boolean,
                              convertView: View?, parent: ViewGroup?): View {
        val view = if (convertView != null) convertView else {
            val layoutInflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            layoutInflater.inflate(R.layout.day_view_group_item, null)
        }
        view.findViewById<TextView>(R.id.categoryText).text = categories[catIndex]
        return view
    }

    override fun getChildrenCount(catIndex: Int): Int {
        return items[categories[catIndex]]!!.size
    }

    override fun getChild(catIndex: Int, itemIndex: Int): Any {
        return items.get(categories[catIndex])!![itemIndex]
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
        val symptom = items[categories[catIndex]]?.get(itemIndex)
        view.day_item.text = symptom?.name ?: ""
        view.item_checkbox.isChecked = symptom?.status ?: false
        return view
    }

    override fun getChildId(catIndex: Int, itemIndex: Int): Long {
        return itemIndex.toLong()
    }

    override fun getGroupCount(): Int {
        return categories.size
    }
}