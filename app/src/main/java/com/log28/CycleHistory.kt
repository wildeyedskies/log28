package com.log28


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


/**
 * A simple [Fragment] subclass.
 * Use the [CycleHistory.newInstance] factory method to
 * create an instance of this fragment.
 */
class CycleHistory : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cycle_history, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment CycleHistory.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(): CycleHistory {
            val fragment = CycleHistory()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

}
