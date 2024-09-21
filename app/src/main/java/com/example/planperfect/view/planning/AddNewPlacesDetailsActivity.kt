package com.example.planperfect.view.planning

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.planperfect.R
import com.example.planperfect.data.model.TouristPlace
import com.example.planperfect.databinding.ActivityAddNewPlacesDetailsBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AddNewPlacesDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNewPlacesDetailsBinding
    private lateinit var selectedPlace: TouristPlace
    private lateinit var tripId: String
    private lateinit var dayId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewPlacesDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the selected place and trip/day IDs from the Intent
        selectedPlace = intent.getParcelableExtra("place") ?: return
        tripId = intent.getStringExtra("tripId") ?: return
        dayId = intent.getStringExtra("dayId") ?: return

        // Set up UI with selected place details...

        binding.buttonSave.setOnClickListener {
            addPlaceToTrip(selectedPlace)
        }
    }

    private fun addPlaceToTrip(place: TouristPlace) {
        Log.d("ADD_NEW_PLACES", selectedPlace.toString())
        Log.d("ADD_NEW_PLACES", tripId.toString())
        Log.d("ADD_NEW_PLACES", dayId.toString())

        // Get the additional details from the UI
        val startTime = binding.editTextStartTime.text.toString().trim()
        val endTime = binding.editTextEndTime.text.toString().trim()
        val notes = binding.editTextNotes.text.toString().trim()

        // Update the place object with the new details
        val updatedPlace = place.copy(startTime = startTime, endTime = endTime, notes = notes)

        // Reference to the itinerary collection for the current trip
        val itineraryCollection = FirebaseFirestore.getInstance()
            .collection("trip")
            .document(tripId)
            .collection("itineraries")

        // Reference to the specific day document (based on dayId)
        val dayDocument = itineraryCollection.document(dayId)

        dayDocument.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                Log.d("ADD_NEW_PLACES", "Day document exists. Attempting to update.")
                dayDocument.update("places", FieldValue.arrayUnion(updatedPlace))
                    .addOnSuccessListener {
                        Toast.makeText(this, "Place added to itinerary", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Log.e("ADD_NEW_PLACES", "Error updating place: ${e.message}")
                        Toast.makeText(this, "Failed to add place: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Log.d("ADD_NEW_PLACES", "Day document does not exist. Creating a new one.")
                val newDayData = hashMapOf("places" to arrayListOf(updatedPlace))
                dayDocument.set(newDayData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Place added to new day itinerary", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Log.e("ADD_NEW_PLACES", "Error creating day and adding place: ${e.message}")
                        Toast.makeText(this, "Failed to create day and add place: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener { e ->
            Log.e("ADD_NEW_PLACES", "Error checking day document: ${e.message}")
            Toast.makeText(this, "Error checking day document: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
