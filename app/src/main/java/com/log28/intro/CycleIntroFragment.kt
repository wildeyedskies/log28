package com.log28.intro


import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_cycle_intro.*
import com.log28.CycleInfo

import com.log28.R


/**
 * A simple [Fragment] subclass.
 * Use the [CycleIntroFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CycleIntroFragment : Fragment() {
    private val realm = Realm.getDefaultInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
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
                realm.executeTransactionAsync {
                    val cycleInfo = it.where(CycleInfo::class.java).findFirst() ?: it.createObject(CycleInfo::class.java)
                    cycleInfo.cycleLength = p0.toString().toIntOrNull() ?: 28
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
                realm.executeTransactionAsync {
                    val cycleInfo = it.where(CycleInfo::class.java).findFirst() ?: it.createObject(CycleInfo::class.java)
                    cycleInfo.periodLength = p0.toString().toIntOrNull() ?: 5
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

}
