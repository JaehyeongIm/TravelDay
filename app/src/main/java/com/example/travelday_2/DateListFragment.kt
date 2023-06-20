package com.example.travelday_2

import DailyScheduleAdapter
import DateListAdapter
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.travelday_2.databinding.FragmentDateListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class DateListFragment : Fragment() {
    lateinit var binding: FragmentDateListBinding
    lateinit var result: String
    private val dates = ArrayList<String>()
    private lateinit var adapter: DateListAdapter
    lateinit var country:String
    lateinit var userId:String
    private val dailyScheduleAdapters = ArrayList<DailyScheduleAdapter>()
    lateinit var icon: ImageView
    lateinit var weatherre: TextView
    lateinit var imgURL:String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= FragmentDateListBinding.inflate(layoutInflater)
        icon = inflater.inflate(R.layout.weather_dlg, container, false).findViewById<ImageView>(R.id.weatherIcon!!)
        weatherre = inflater.inflate(R.layout.weather_dlg, container, false).findViewById<TextView>(R.id.weahterResult!!)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getWeather()
        init()
        initRecyclerView()
        initBackStack()

    }
    //환율 버튼과 날씨 버튼 눌렀을 때 구현
    private fun init(){
        result  = "weather"
        binding.weatherLayout.setOnClickListener {
                showWeatherDialog()
        }
        binding.exchangeLayout.setOnClickListener {
            val country = arguments?.getString("클릭된 국가")

            val bundle = Bundle().apply {
                putString("클릭된 국가", country)
            }

            val exchangeFragment=ExchangeRateFragment().apply {
                arguments=bundle
            }
            parentFragmentManager.beginTransaction().apply {
                add(R.id.frag_container, exchangeFragment)
                hide(this@DateListFragment)
                addToBackStack(null)
                commit()
            }
        }
    }

    private fun getWeather(){
        val country = arguments?.getString("클릭된 국가")
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
    // 뒤로가기 버튼이 눌렸을 때 처리할 동작 구현
    private fun initBackStack() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    @SuppressLint("SuspiciousIndentation", "SetTextI18n")
    private fun initRecyclerView() {
        userId = FirebaseAuth.getInstance().currentUser?.uid!!
        country = arguments?.getString("클릭된 국가")!!

        if (country != null && userId != null) {
            adapter = DateListAdapter(requireContext(), userId!!, country!!, ArrayList())
            binding.recyclerView.adapter = adapter
            binding.recyclerView.layoutManager = LinearLayoutManager(context,
                LinearLayoutManager.VERTICAL,false)
            getEvent()

        }
            adapter.itemClickListener = object : DateListAdapter.OnItemClickListener {
                override fun onItemClick(data: String) {
                    val bundle = Bundle().apply {
                        putString("클릭된 국가", country)
                        putString("클릭된 날짜", data)
                    }

                    val dailyScheduleAddFragment = DailyScheduleAddFragment().apply {
                        arguments = bundle
                    }

                    parentFragmentManager.beginTransaction().apply {
                        add(R.id.frag_container, dailyScheduleAddFragment)
                        hide(this@DateListFragment)
                        addToBackStack(null)
                        commit()
                    }
                }

                override fun onOutfitClick(data: String) {
                    val fragment = OutfitFragment()
                    val bundle = Bundle()
                    bundle.putString("클릭된 국가", country)
                    bundle.putString("클릭된 날짜", data)
                    fragment.arguments = bundle // Bundle을 Fragment에 설정합니다.

                    val fragmentManager = parentFragmentManager
                    val fragmentTransaction = fragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.frag_container, fragment)
                    fragmentTransaction.addToBackStack("OutfitFragment")
                    fragmentTransaction.commit()
                }
            }

            val simpleCallback = object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    adapter.moveItem(viewHolder.adapterPosition, target.adapterPosition)
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    adapter.removeItem(viewHolder.adapterPosition)
                }
            }
            val itemTouchHelper = ItemTouchHelper(simpleCallback)
            itemTouchHelper.attachToRecyclerView(binding.recyclerView)
        }

    private fun getEvent() {
        val dateRef = DBRef.userRef.child(userId!!).child(country!!)
        dateRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updatedDates = ArrayList<String>()
                snapshot.children.forEach { dateSnapshot ->
                    val date = dateSnapshot.key
                    date?.let { updatedDates.add(it) }
                }
                adapter.items.clear()
                adapter.items.addAll(updatedDates)

                snapshot.children.forEach { dateSnapshot ->
                    val dailyItems = ArrayList<DailyItem>()
                    dateSnapshot.children.forEach { itemSnapshot ->
                        val item = itemSnapshot.getValue(DailyItem::class.java)
                        item?.let { dailyItems.add(it) }
                    }
                    val innerAdapter = DailyScheduleAdapter(arrayListOf())
                    innerAdapter.updateItems(dailyItems)
                    dailyScheduleAdapters.add(innerAdapter)
                }
                //여기서 부터 상단 탭 구현

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

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error here
            }
        })
    }

}



////상단 탭에 국가이름, 날짜 데이터 및 디데이 표시
//val startDate = country.dateList.firstOrNull()?.date
//val endDate = country.dateList.lastOrNull()?.date
//val travelPeriod = if (startDate != null && endDate != null) {
//    "$startDate ~ $endDate"
//} else {
//    ""
//}
//binding.travelData.text=country.name +"\n " +travelPeriod
//binding.dDayDateList.text="D-"+country.dDay


