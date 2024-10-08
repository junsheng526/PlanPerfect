package com.example.planperfect.view.planning.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.planperfect.view.planning.CollaboratorsFragment
import com.example.planperfect.view.planning.TripDetailsFragment

class TripDetailsPagerAdapter(activity: FragmentActivity, private val tripId: String) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2  // Number of tabs

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TripDetailsFragment.newInstance(tripId)  // Trip Details tab
            1 -> CollaboratorsFragment.newInstance(tripId)  // Collaborators tab
            else -> throw IllegalStateException("Invalid position")
        }
    }
}