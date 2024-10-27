package com.example.planperfect.view.planning

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.planperfect.databinding.ActivityFilterItineraryBinding
import java.util.Calendar

class FilterItineraryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFilterItineraryBinding
    private var selectedDate: String? = null
    private var selectedStatus: String? = null
    private var selectedRole: String? = null
    val statuses = listOf("-","Ongoing", "Pending", "Completed")
    val roles = listOf("-","owner", "editor", "viewer")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterItineraryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dateFilter = intent.getStringExtra("FILTER_DATE")
        val statusFilter = intent.getStringExtra("FILTER_STATUS")
        val roleFilter = intent.getStringExtra("FILTER_ROLE")

        selectedDate = dateFilter
        selectedStatus = statusFilter ?: statuses[0]
        selectedRole = roleFilter ?: roles[0]
        // Initialize UI fields based on existing filter values
        binding.editTextStartDate.setText(dateFilter)

        setupToolbar()
        setupDatePicker()
        setupSpinners()

        // Apply Button - Return filter data to PlanningFragment
        binding.applyBtn.setOnClickListener {
            val intent = Intent()
            intent.putExtra("FILTER_DATE", selectedDate)
            intent.putExtra("FILTER_STATUS", selectedStatus)
            intent.putExtra("FILTER_ROLE", selectedRole)
            setResult(RESULT_OK, intent)
            finish()
        }

        // Reset Button - Clear filters
        binding.resetBtn.setOnClickListener {
            selectedDate = null
            selectedStatus = statuses[0]
            selectedRole = roles[0]

            // Update UI elements to reflect cleared values
            binding.editTextStartDate.text?.clear()
            binding.spinnerStatus.setSelection(0)
            binding.spinnerRole.setSelection(0)

            intent.putExtra("FILTER_DATE", selectedDate)
            intent.putExtra("FILTER_STATUS", selectedStatus)
            intent.putExtra("FILTER_ROLE", selectedRole)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun setupDatePicker() {
        binding.editTextStartDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = "${selectedDay}/${selectedMonth + 1}/${selectedYear}"
                Log.d("selectedDate", selectedDate!!)
                binding.editTextStartDate.setText(selectedDate)
            }, year, month, day)

            datePickerDialog.show()
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

    private fun setupSpinners() {
        // Configure status spinner
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statuses)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = statusAdapter

        // Set spinner to the current selected status
        binding.spinnerStatus.setSelection(statuses.indexOf(selectedStatus))

        // Configure role spinner
        val roleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRole.adapter = roleAdapter

        // Set spinner to the current selected role
        binding.spinnerRole.setSelection(roles.indexOf(selectedRole))

        // Set OnItemSelectedListener for each spinner
        binding.spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedStatus = statuses[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.spinnerRole.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedRole = roles[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}
