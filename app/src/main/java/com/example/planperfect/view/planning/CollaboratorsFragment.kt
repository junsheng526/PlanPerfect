package com.example.planperfect.view.planning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.planperfect.R

class CollaboratorsFragment : Fragment() {

    companion object {
        private const val ARG_TRIP_ID = "tripId"

        fun newInstance(tripId: String): CollaboratorsFragment {
            val fragment = CollaboratorsFragment()
            val args = Bundle()
            args.putString(ARG_TRIP_ID, tripId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_collaborators, container, false)
    }
}
