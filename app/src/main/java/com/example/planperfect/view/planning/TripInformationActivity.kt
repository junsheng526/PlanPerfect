package com.example.planperfect.view.planning

import android.R
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.planperfect.databinding.ActivityTripInformationBinding
import com.example.planperfect.view.MainActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class TripInformationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTripInformationBinding
    private var startDate: String? = null
    private var endDate: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTripInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Open DatePickerDialog on clicking Start Date
        binding.editTextStartDate.setOnClickListener {
            showDatePickerDialog(binding.editTextStartDate, isStartDate = true)
        }

        // Open DatePickerDialog on clicking End Date
        binding.editTextEndDate.setOnClickListener {
            showDatePickerDialog(binding.editTextEndDate, isStartDate = false)
        }

        setupToolbar()

        binding.nextBtn.setOnClickListener {
            // Passing the start and end date to the next activity
            val tripName = binding.editTextTripName.text.toString()
            Log.d("NEXT_BUTTON", tripName)
            val intent = Intent(this, TripLocationActivity::class.java).apply {
                putExtra("startDate", startDate)
                putExtra("endDate", endDate)
                putExtra("tripName", tripName)
            }
            startActivity(intent)
        }
    }

    private fun setupToolbar() {
        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(binding.toolbar)
        toolbar.setNavigationOnClickListener {
            this.onBackPressed()
        }

        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun showDatePickerDialog(editText: EditText, isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = dateFormat.format(selectedDate.time)
                editText.setText(date)

                if (isStartDate) {
                    startDate = date
                } else {
                    endDate = date
                }
            },
            year, month, day
        )

        // Optional: Add validation if the user is selecting an end date after the start date
        if (!isStartDate && startDate != null) {
            val startCalendar = Calendar.getInstance().apply {
                val parts = startDate!!.split("/")
                set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
            }
            datePickerDialog.datePicker.minDate = startCalendar.timeInMillis
        }

        datePickerDialog.show()
    }
}