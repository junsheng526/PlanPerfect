package com.example.planperfect.view.planning

import android.R
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.planperfect.databinding.ActivityTripInformationBinding
import java.util.Calendar


class TripInformationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTripInformationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTripInformationBinding.inflate(layoutInflater)

        // Open DatePickerDialog on clicking Start Date
        binding.editTextStartDate.setOnClickListener {
            showDatePickerDialog(binding.editTextStartDate)
        }

        // Open DatePickerDialog on clicking End Date
        binding.editTextEndDate.setOnClickListener {
            showDatePickerDialog(binding.editTextEndDate)
        }

//        setSupportActionBar(binding.toolbar)
//
//        if (supportActionBar != null) {
//            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
//        }
    }

    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Set selected date in the EditText
                editText.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
            },
            year, month, day
        )
        datePickerDialog.show()
    }
}