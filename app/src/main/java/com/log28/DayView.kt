package com.log28

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.xwray.groupie.ExpandableGroup
import devs.mulham.horizontalcalendar.HorizontalCalendar
import java.util.*
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener
import kotlinx.android.synthetic.main.fragment_day_view.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.*
import com.log28.groupie.ChildItem
import com.log28.groupie.ExpandableHeaderItem
import com.log28.groupie.NotesItem
import devs.mulham.horizontalcalendar.utils.Utils
import io.realm.OrderedCollectionChangeSet
import io.realm.Realm

/**
 * Handles the day view
 */
class DayView : Fragment() {
    // changes both the data displayed and the date at the top.
    lateinit var navigateToDay: (c: Calendar) -> Unit
    // update the day if the day has changed
    lateinit var updateToday: () -> Unit
    // the day currently displayed in the view
    private var currentDay = Calendar.getInstance()
    // the current day when the activity was created
    private val initalToday = Calendar.getInstance()

    private val realm = Realm.getDefaultInstance()

    private val categories = realm.getActiveCategories()
    private val symptoms = realm.getActiveSymptoms()

    private val groupAdapter = GroupAdapter<GroupieViewHolder>()

    // we store our categories and symptom groups here so we can update them
    private val categoryGroup = mutableListOf<ExpandableGroup>()
    private val symptomList = mutableListOf<MutableList<ChildItem>>()
    // reference to the notes and sleep amount
    private var notesAndSleep = Section()
    private lateinit var notesItem: NotesItem

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_day_view, container, false)
        setupHorizontalCalendar(rootView)
        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // set currentDay if we received it
        currentDay = savedInstanceState?.getSerializable("day") as? Calendar
                ?: Calendar.getInstance()

        // if a category is enabled or disabled, redraw everything
        categories.addChangeListener{
            _, changeSet -> redrawRecyclerView(changeSet)
        }
        symptoms.addChangeListener{
            _, changeSet -> redrawRecyclerView(changeSet)
        }

        setupRecyclerView()
    }

    override fun onDestroy() {
        super.onDestroy()
        categories.removeAllChangeListeners()
        symptoms.removeAllChangeListeners()
        realm.close()
    }

    // if the day has changed
    override fun onResume() {
        super.onResume()
        if (Utils.daysBetween(initalToday, Calendar.getInstance()) != 0 &&
                this::updateToday.isInitialized)
            updateToday.invoke()
    }

    // setup the recycler view
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDayText(currentDay)

        day_view_recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = groupAdapter
        }
    }

    private fun setupHorizontalCalendar(rootView: View) {
        // setup the top calendar
        val startDate = currentDay.clone() as Calendar
        startDate.add(Calendar.MONTH, -1)
        val endDate = Calendar.getInstance()

        val currentdate = Calendar.getInstance()

        val horizontalCalendar = HorizontalCalendar.Builder(rootView, R.id.top_calendar)
                .defaultSelectedDate(currentdate)
                .range(startDate, endDate)
                .datesNumberOnScreen(5).build()

        updateToday = {
            Log.d(TAG, "day pass detected, updating day view")
            horizontalCalendar.setRange(startDate, Calendar.getInstance())
            navigateToDay.invoke(Calendar.getInstance())
        }

        navigateToDay = {
            c -> // set the range to be one month before
            //TODO clean this mess up
            if (c.before(startDate)) {
                startDate.set(Calendar.YEAR, c.get(Calendar.YEAR))
                startDate.set(Calendar.MONTH, c.get(Calendar.MONTH))
                startDate.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH))
                startDate.add(Calendar.MONTH, -1)

                Log.d(TAG, "navtoday seting startdate to ${startDate.formatDate()}")
                horizontalCalendar.setRange(startDate, endDate)
                horizontalCalendar.refresh()
            }
            horizontalCalendar.selectDate(c, true)
        }

        horizontalCalendar.calendarListener = object : HorizontalCalendarListener() {
            override fun onDateSelected(date: Calendar, position: Int) {
                Log.d(TAG, "horizontal calendar date set to ${date.formatDate()}")
                loadDayData(date)
                // this little bit of code extends the range of the dates
                val cDate = date.clone() as Calendar
                cDate.add(Calendar.DAY_OF_YEAR, -5)
                if (startDate.after(cDate)) {
                    Log.d(TAG, "setting range")
                    startDate.add(Calendar.MONTH, -1)

                    horizontalCalendar.setRange(startDate, endDate)
                    horizontalCalendar.refresh()
                }
            }
        }
    }


    // this gets called when a symptom or category is changed
    private fun redrawRecyclerView(changeSet: OrderedCollectionChangeSet?) {
        if (changeSet != null) {
            Log.d(TAG, "categories updated $changeSet")

            groupAdapter.clear()
            categoryGroup.clear()
            symptomList.clear()
            notesAndSleep = Section()

            setupRecyclerView()
        }
    }

    private fun setupRecyclerView() {
        val daydata = realm.getDataByDate(currentDay)

        // add each category as a header
        // add each symptom under a category, set the state based on what's in the DayData object
        categories.forEach { category ->
            ExpandableGroup(ExpandableHeaderItem(category.name)).apply {
                val symptomsInCategory = mutableListOf<ChildItem>()
                symptoms.filter { s -> s.category?.name == category.name }.forEach { symptom ->
                    val childItem = ChildItem(symptom, symptom in daydata.symptoms,
                            // here we pass an update function
                            { daydata.toggleSymptom(context, symptom) })

                    symptomsInCategory.add(childItem)
                }
                symptomList.add(symptomsInCategory)
                this.addAll(symptomsInCategory)
                categoryGroup.add(this)
            }
        }

        notesItem = NotesItem(daydata)
        notesAndSleep.add(notesItem)

        groupAdapter.addAll(categoryGroup)
        groupAdapter.add(notesAndSleep)
    }

    private fun loadDayData(day: Calendar) {
        currentDay = day
        Log.d(TAG, "Loading data for ${day.formatDate()}")

        // set the day text
        setDayText(day)

        val daydata = realm.getDataByDate(currentDay)

        symptomList.forEach {
            it.forEach {
                it.onClick = { daydata.toggleSymptom(context, it.symptom) }
                it.state = it.symptom in daydata.symptoms
            }
        }
        //TODO can we avoid redrawing everything
        categoryGroup.forEach {
            it.notifyChanged()
        }

        notesItem.daydata = daydata
        notesAndSleep.notifyChanged()
    }

    private fun setDayText(day: Calendar) {
        val now = Calendar.getInstance()
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_MONTH, -1)

        if (Utils.isSameDate(day, now))
            day_text.text = context!!.getString(R.string.today)
        else if (Utils.isSameDate(day, yesterday))
            day_text.text = context!!.getString(R.string.yesterday)
        else
            day_text.text = context!!.getString(R.string.days_ago, Utils.daysBetween(day, now))
    }

    companion object {
        const val TAG = "DAYVIEW"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment DayView.
         */
        fun newInstance(day: Calendar): DayView {
            val fragment = DayView()
            val args = Bundle()
            args.putSerializable("day", day)
            fragment.arguments = args
            return fragment
        }
    }
}