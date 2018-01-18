package org.mcxa.log28.org.mcxa.log28.intro


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

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
