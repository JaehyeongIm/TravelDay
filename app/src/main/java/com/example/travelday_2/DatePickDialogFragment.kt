package com.example.travelday_2

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import com.example.travelday_2.databinding.FragmentDatePickDialogBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class DatePickDialogFragment : Fragment() {
    lateinit var binding:FragmentDatePickDialogBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentDatePickDialogBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showDataRangePicker()
        initBackStack()
    }

    private fun initBackStack() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 뒤로가기 버튼이 눌렸을 때 처리할 동작 구현
                parentFragmentManager.popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun showDataRangePicker() {
        val selectedCountry=arguments?.getString("country")

        val dateRangePicker =
            MaterialDatePicker
                .Builder.dateRangePicker()
                .setTitleText("Select Date")
                .build()

        dateRangePicker.show(
            activity?.supportFragmentManager!!,
            "date_range_picker"
        )

        dateRangePicker.addOnPositiveButtonClickListener { dateSelected ->
            val startDate = dateSelected.first
            val endDate = dateSelected.second

            // Convert Long to Date object
            val startDateAsDate = Date(startDate)
            val endDateAsDate = Date(endDate)

            // Get the list of dates between start and end
            val dates = getDatesBetween(startDateAsDate, endDateAsDate)

            // 데이터베이스에 데이터 저장

            val userId = FirebaseAuth.getInstance().currentUser
            val uid = userId?.uid

            dates.forEach { date ->
                if (uid != null) {
                    DBRef.writeDataToDatabase(uid, selectedCountry!!, date)
                }
            }
            val bundle=Bundle().apply {
                putStringArrayList("DateList",dates)
                putString("클릭된 국가",selectedCountry)
            }
            val travelListFragment=TravelListFragment().apply {
                arguments=bundle
            }
            parentFragmentManager.beginTransaction().apply {
                add(R.id.frag_container,travelListFragment)
                hide(this@DatePickDialogFragment)
                show(travelListFragment)
                commit()
            }
        }

        dateRangePicker.addOnNegativeButtonClickListener {
            Handler(Looper.getMainLooper()).postDelayed({
                showDataRangePicker()
            }, 500) // Wait for 500 milliseconds
        }
    }

    private fun getDatesBetween(startDate: Date, endDate: Date): ArrayList<String> {
        val dateList = arrayListOf<String>()
        val calendar = Calendar.getInstance()
        calendar.time = startDate

        while (calendar.time.before(endDate) || calendar.time.equals(endDate)) {
            val result = calendar.time
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = format.format(result)
            dateList.add(formattedDate)
            calendar.add(Calendar.DATE, 1)
        }
        return dateList
    }

}
