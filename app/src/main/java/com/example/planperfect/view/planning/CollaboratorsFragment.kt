package com.example.planperfect.view.planning

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.planperfect.data.model.User
import com.example.planperfect.databinding.FragmentCollaboratorsBinding
import com.example.planperfect.view.planning.adapter.CollaboratorsAdapter
import com.example.planperfect.viewmodel.AuthViewModel
import com.example.planperfect.viewmodel.CollaboratorViewModel

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

    private lateinit var binding: FragmentCollaboratorsBinding
    private lateinit var collaboratorsAdapter: CollaboratorsAdapter
    private var collaboratorsList = mutableListOf<Pair<User, String>>() // Pair of User and Role

    // Instantiate both AuthViewModel and CollaboratorViewModel
    private val authViewModel: AuthViewModel by viewModels()
    private val collaboratorViewModel: CollaboratorViewModel by lazy {
        CollaboratorViewModel(authViewModel) // Passing AuthViewModel to CollaboratorViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCollaboratorsBinding.inflate(inflater, container, false)

        val tripId = arguments?.getString(ARG_TRIP_ID) ?: return binding.root

        // Set up RecyclerView
        binding.collaboratorsRecyclerView.layoutManager = LinearLayoutManager(context)
        collaboratorsAdapter = CollaboratorsAdapter(collaboratorsList)
        binding.collaboratorsRecyclerView.adapter = collaboratorsAdapter

        // Observe the LiveData from CollaboratorViewModel
        collaboratorViewModel.collaboratorsWithUserDetailsLiveData.observe(viewLifecycleOwner, Observer { collaboratorsWithDetails ->
            collaboratorsList.clear()
            collaboratorsList.addAll(collaboratorsWithDetails)
            collaboratorsAdapter.notifyDataSetChanged()
        })

        // Fetch collaborators with user details for the specific trip
        collaboratorViewModel.getCollaboratorsWithUserDetails(tripId)

        binding.addCollaboratorButton.setOnClickListener {
            val intent = Intent(requireContext(), AddCollaboratorActivity::class.java)
            intent.putExtra("tripId", tripId)
            startActivity(intent)
        }

        return binding.root
    }
}
