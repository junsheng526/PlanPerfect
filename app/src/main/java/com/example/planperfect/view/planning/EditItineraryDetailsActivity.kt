package com.example.planperfect.view.planning

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.planperfect.data.model.TouristPlace
import com.example.planperfect.databinding.ActivityEditIteneraryDetailsBinding
import com.example.planperfect.viewmodel.TripViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditItineraryDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditIteneraryDetailsBinding
    private lateinit var selectedPlace: TouristPlace
    private lateinit var tripId: String
    private lateinit var dayId: String
    private var placePosition: Int = -1
    private var startTimeCalendar: Calendar? = null
    private var endTimeCalendar: Calendar? = null
    private lateinit var viewModel: TripViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditIteneraryDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()

        viewModel = ViewModelProvider(this).get(TripViewModel::class.java)

        // Get the selected place and trip/day IDs from the Intent
        selectedPlace = intent.getParcelableExtra("place") ?: return
        tripId = intent.getStringExtra("tripId") ?: return
        dayId = intent.getStringExtra("dayId") ?: return
        placePosition = intent.getIntExtra("placePosition", -1)

        // Populate existing data in the UI
        populateExistingData()

        // Set up time picker dialogs for start and end times
        binding.editTextStartTime.setOnClickListener {
            showTimePicker { selectedTime, calendar ->
                binding.editTextStartTime.text =
                    Editable.Factory.getInstance().newEditable(selectedTime)
                startTimeCalendar = calendar
            }
        }

        binding.editTextEndTime.setOnClickListener {
            showTimePicker { selectedTime, calendar ->
                binding.editTextEndTime.text =
                    Editable.Factory.getInstance().newEditable(selectedTime)
                endTimeCalendar = calendar
            }
        }

        // Save changes button
        binding.buttonSave.setOnClickListener {
            if (validateTimes()) {
                checkForOverlapAndUpdatePlace()
            }
        }
    }

    private fun populateExistingData() {
        Log.d("EditItineraryDetailsActivity", "Selected Place: $selectedPlace")
        binding.editTextStartTime.text =
            Editable.Factory.getInstance().newEditable(selectedPlace.startTime)
        binding.editTextEndTime.text =
            Editable.Factory.getInstance().newEditable(selectedPlace.endTime)
        binding.editTextNotes.setText(selectedPlace.notes)
        // Initialize startTimeCalendar and endTimeCalendar based on existing start and end times
        try {
            startTimeCalendar = Calendar.getInstance().apply {
                time =
                    SimpleDateFormat("hh:mm a", Locale.getDefault()).parse(selectedPlace.startTime)
                        ?: time
            }
            endTimeCalendar = Calendar.getInstance().apply {
                time = SimpleDateFormat("hh:mm a", Locale.getDefault()).parse(selectedPlace.endTime)
                    ?: time
            }
        } catch (e: Exception) {
            Log.e("EditItineraryDetailsActivity", "Error parsing time: ${e.message}")
            Toast.makeText(
                this,
                "Error loading times. Please check the format.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showTimePicker(onTimeSelected: (String, Calendar) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
            calendar.set(Calendar.MINUTE, selectedMinute)
            val formattedTime =
                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)
            onTimeSelected(formattedTime, calendar)
        }, hour, minute, false)

        timePickerDialog.show()
    }

    private fun checkForOverlapAndUpdatePlace() {
        val startTime = binding.editTextStartTime.text.toString().trim()
        val endTime = binding.editTextEndTime.text.toString().trim()

        lifecycleScope.launch {
            val hasOverlap = viewModel.checkTimeOverlap(
                tripId,
                dayId,
                startTime,
                endTime,
                excludePlaceId = selectedPlace.id
            )

            if (hasOverlap) {
                Toast.makeText(
                    this@EditItineraryDetailsActivity,
                    "Time overlap with existing place",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                updatePlaceDetails() // No overlap, proceed with update
            }
        }
    }

    private fun updatePlaceDetails() {
        val startTime = binding.editTextStartTime.text.toString().trim()
        val endTime = binding.editTextEndTime.text.toString().trim()
        val notes = binding.editTextNotes.text.toString().trim()

        val updatedPlace = selectedPlace.copy(
            startTime = startTime,
            endTime = endTime,
            notes = notes
        )


        lifecycleScope.launch {
            val isSuccess = viewModel.updatePlace(tripId, dayId, updatedPlace, placePosition)

            if (isSuccess) {
                Toast.makeText(
                    this@EditItineraryDetailsActivity,
                    "Updated successfully!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@EditItineraryDetailsActivity,
                    "Something went wrong. Please try again!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun validateTimes(): Boolean {
        return if (startTimeCalendar != null && endTimeCalendar != null) {
            if (startTimeCalendar!!.before(endTimeCalendar)) {
                true
            } else {
                Toast.makeText(this, "Start time must be earlier than end time", Toast.LENGTH_SHORT)
                    .show()
                false
            }
        } else {
            Toast.makeText(this, "Please select both start and end times", Toast.LENGTH_SHORT)
                .show()
            false
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            this.onBackPressed()
        }
    }
}
