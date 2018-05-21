package com.log28

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import com.log28.groupie.OptionHeader
import com.log28.groupie.OptionItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.activity_options.*

// enable and disable the individual tracking options
class OptionsActivity : AppCompatActivity() {
    private val symptoms = getSymptoms()
    private val categories = getCategories()

    private val groupAdapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)
        setSupportActionBar(toolbar)
        // draw the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        setupRecyclerView()

        /*add_symptom.setOnClickListener {

        }

        options_recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy < 0 && !add_symptom.isShown)
                    add_symptom.show()
                else if (dy > 0 && add_symptom.isShown)
                    add_symptom.hide()
            }
        })*/
    }

    // exit when the back button is pressed
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // handle arrow click here
        if (item.itemId == android.R.id.home) {
            finish() // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerView() {
        val categoryGroup = mutableListOf<Section>()
        // add each category as a header
        // add each symptom under a category, set the state based on what's in the DayData object
        categories.forEach { category ->
            Section(OptionHeader(category.name)).apply {
                val symptomsInCategory = mutableListOf<OptionItem>()
                symptoms.filter { s -> s.category?.name == category.name }.forEach { symptom ->
                    val optionItem = OptionItem(symptom)

                    symptomsInCategory.add(optionItem)
                }
                this.addAll(symptomsInCategory)
                categoryGroup.add(this)
            }
        }

        groupAdapter.addAll(categoryGroup)

        options_recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = groupAdapter
        }
    }
}
