package com.example.travelday_2

import DailyScheduleAdapter
import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.travelday_2.databinding.FragmentDateListBinding
import com.example.travelday_2.databinding.FragmentExchangeRateBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutionException


class ExchangeRateFragment : Fragment() {
    lateinit var result :String
    val scope = CoroutineScope(Dispatchers.IO)
    lateinit var icon:ImageView
    lateinit var weatherre:TextView
    lateinit var imgURL:String
    lateinit var binding:FragmentExchangeRateBinding
    private val currencyList = arrayOf("KRW", "USD", "EUR", "CAD")
    private lateinit var et_from: TextView
    private lateinit var tv_to: TextView
    private lateinit var btn_exchange: Button
    private val fromto = arrayOfNulls<String>(2)
    private lateinit var tv_test: TextView
    private var currencyRate = 0.0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= FragmentExchangeRateBinding.inflate(layoutInflater)
        icon = inflater.inflate(R.layout.weather_dlg, container, false).findViewById<ImageView>(R.id.weatherIcon!!)
        weatherre = inflater.inflate(R.layout.weather_dlg, container, false).findViewById<TextView>(R.id.weahterResult!!)

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        initBackStack()
        getEvent()

    }

    fun getName(c : String):String{
        var li = mutableMapOf<String,String>()
        li.put("서울","seoul")
        li.put("한국","korea")
        li.put("일본","japan")
        li.put("도쿄","tokyo")
        li.put("미국","america")
        li.put("인천","incheon")
        li.put("중국","china")
        li.put("프랑스","france")
        li.put("파리","paris")
        li.put("상하이","shanghai")
        li.put("영국","england")
        li.put("런던","london")
        li.put("베를린","berlin")
        li.put("독일","germany")
        li.put("마드리드","madrid")
        li.put("로마","rome")
        for(i in li.keys){
            if(i.equals(c)){
                return li.get(i)!!
            }
        }
        return c
    }
    private fun getEvent() {
        var userId = FirebaseAuth.getInstance().currentUser?.uid!!
        var country = arguments?.getString("클릭된 국가")!!

        val dateRef = DBRef.userRef.child(userId!!).child(country!!)
        dateRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updatedDates = ArrayList<String>()
                snapshot.children.forEach { dateSnapshot ->
                    val date = dateSnapshot.key
                    date?.let { updatedDates.add(it) }
                }




                // 날짜 데이터를 정렬합니다
                updatedDates.sort()
                // 시작 날짜와 종료 날짜를 가져옵니다
                val startDate = updatedDates.firstOrNull()
                val endDate = updatedDates.lastOrNull()
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val startDateFormat = format.parse(startDate)
                val current = Calendar.getInstance().apply {
                    // Remove the time part for accurate day difference
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                val diff = startDateFormat.time - current.time
                val dday = Math.ceil(diff.toDouble() / (24 * 60 * 60 * 1000)).toInt()
                val travelPeriod = if (startDate != null && endDate != null) {
                    "$startDate ~ $endDate"
                } else {
                    ""
                }
                binding.travelData.text = "$country\n$travelPeriod"
                binding.dDayDateList.text = "D-$dday"


            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error here
            }
        })
    }
    private fun init(){
        result  = "weather"
        getWeather()
        binding.weatherLayout.setOnClickListener {


            showWeatherDialog()
        }

        val spinner = binding.spinner
        val spinner2 = binding.spinner2

        et_from = binding.etFrom
        tv_to = binding.tvTo

        btn_exchange = binding.btnExchange

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, currencyList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, i: Int, l: Long) {
                fromto[0] = currencyList[i]
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {

            }
        }

        spinner2.adapter = adapter
        spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, i: Int, l: Long) {
                fromto[1] = currencyList[i]
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {

            }
        }

        btn_exchange.setOnClickListener {
            GlobalScope.launch {
                try {
                    val task = Task()
                    currencyRate = task.executeAsync(*fromto)

                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                }

                val input = et_from.text.toString().toDouble()
                val result = Math.round(input * currencyRate * 100.0) / 100.0

                tv_to.text = result.toString()
            }

        }
    }

    private fun getWeather(){
        val country = arguments?.getString("클릭된 국가")
        //getName(country!!)
        val requestQueue = Volley.newRequestQueue(requireContext())
        val url = "http://api.openweathermap.org/data/2.5/weather?q="+getName(country!!)+"&appid="+"d74c3bbee7a3c497383271ff0d494542"

        val stringRequest = StringRequest(
            Request.Method.GET,url,
            {
                    response ->
                val jsonObject = JSONObject(response)

                val weatherJson = jsonObject.getJSONArray("weather")
                val weatherObj = weatherJson.getJSONObject(0)

                var weather = weatherObj.getString("description")
                imgURL = "http://openweathermap.org/img/w/" + weatherObj.getString("icon") + ".png"
                //val imgURL = "http://openweathermap.org/img/w/" + weatherObj.getString("icon") + ".png"
                //Glide.with(this).load(imgURL).into(findViewById<ImageView>(R.id.weatherIcon))
                val tempK = JSONObject(jsonObject.getString("main"))
                val tempDo = (Math.round((tempK.getDouble("temp")-273.15)*100)/100.0)
                result = tempDo.toString() +"°C\n"+weather

                //binding.result.text = weather

                weatherre.text = result

            },
            {
                Log.i("weahter",it.message.toString())
                result = "error"
            })

        requestQueue.add(stringRequest)
    }

    //weather dialog
    private fun showWeatherDialog() {
        getWeather()
        //val dialogView = LayoutInflater.from(context).inflate(R.layout.fragment_date_list, null)

        val country = arguments?.getString("클릭된 국가")

        var dlg = Dialog(requireContext())
        dlg.setContentView(R.layout.weather_dlg)

        var tv = dlg.findViewById<TextView>(R.id.weahterResult)
        var iv = dlg.findViewById<ImageView>(R.id.weatherIcon)
        var n = dlg.findViewById<TextView>(R.id.counName)
        n.text = country
        tv.text = result
        Glide.with(this).load(imgURL).into(iv)
        dlg.show()

//        val dialogBuilder = AlertDialog.Builder(requireContext())
//
//            .setTitle(country.name)
//            .setMessage(weatherre.text)
//
//            .setPositiveButton("확인", null)
//            .setNegativeButton("취소") { dialog, _ ->
//                dialog.cancel()
//            }
//
//        val dialog = dialogBuilder.create()
//        dialog.show()





    }


    // 뒤로가기 버튼이 눌렸을 때 처리할 동작 구현
    private fun initBackStack() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }


}



