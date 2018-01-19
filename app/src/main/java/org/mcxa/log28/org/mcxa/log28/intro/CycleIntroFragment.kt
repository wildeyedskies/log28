package org.mcxa.log28.org.mcxa.log28.intro


import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_cycle_intro.*

import org.mcxa.log28.R


/**
 * A simple [Fragment] subclass.
 * Use the [CycleIntroFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CycleIntroFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cycle_intro, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cycle_length_input.addTextChangedListener(object :TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // we don't care
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // we don't care
            }

            override fun afterTextChanged(p0: Editable?) {
                AsyncTask.execute {
                    val edit = PreferenceManager.getDefaultSharedPreferences(this@CycleIntroFragment.context).edit()
                    val cycle = if (p0.toString() == "") "28" else p0.toString()
                    edit.putString("cycle_length", cycle)
                    edit.apply()
                }
            }
        })

        period_length_input.addTextChangedListener(object :TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // we don't care
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // we don't care
            }

            override fun afterTextChanged(p0: Editable?) {
                AsyncTask.execute {
                    val edit = PreferenceManager.getDefaultSharedPreferences(this@CycleIntroFragment.context).edit()
                    val period = if (p0.toString() == "") "5" else p0.toString()
                    edit.putString("period_length", period)
                    edit.apply()
                }
            }
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment CycleIntroFragment.
         */
        fun newInstance(): CycleIntroFragment {
            val fragment = CycleIntroFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

}// Required empty public constructor
