package com.example.travelday_2

import DailyScheduleAdapter
import DateListAdapter
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
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


class DateListFragment : Fragment() {
    lateinit var binding: FragmentDateListBinding
    lateinit var result: String
    private val dates = ArrayList<String>()
    private lateinit var adapter: DateListAdapter
    lateinit var country:String
    lateinit var userId:String
    private val dailyScheduleAdapters = ArrayList<DailyScheduleAdapter>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= FragmentDateListBinding.inflate(layoutInflater)
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
        val url = "http://api.openweathermap.org/data/2.5/weather?q="+country+"&appid="+"d74c3bbee7a3c497383271ff0d494542"

        val stringRequest = StringRequest(
            Request.Method.GET,url,
            {
                    response ->
                val jsonObject = JSONObject(response)

                val weatherJson = jsonObject.getJSONArray("weather")
                val weatherObj = weatherJson.getJSONObject(0)

                var weather = weatherObj.getString("description")
                //val imgURL = "http://openweathermap.org/img/w/" + weatherObj.getString("icon") + ".png"
                //Glide.with(this).load(imgURL).into(findViewById<ImageView>(R.id.weatherIcon))
                val tempK = JSONObject(jsonObject.getString("main"))
                val tempDo = (Math.round((tempK.getDouble("temp")-273.15)*100)/100.0)
                weather = weather + tempDo + "°C"
                //binding.result.text = weather
                result = weather

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

        val dialogBuilder = AlertDialog.Builder(requireContext())

            .setTitle(country)
            .setMessage(result)
            .setPositiveButton("확인", null)
            .setNegativeButton("취소") { dialog, _ ->
                dialog.cancel()
            }

        val dialog = dialogBuilder.create()
        dialog.show()



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
                    val fragmentManager = parentFragmentManager
                    val fragmentTransaction = fragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.frag_container, fragment)
                    fragmentTransaction.addToBackStack(null)
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
                    val innerAdapter = DailyScheduleAdapter(dailyItems)
                    dailyScheduleAdapters.add(innerAdapter)
                }
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


