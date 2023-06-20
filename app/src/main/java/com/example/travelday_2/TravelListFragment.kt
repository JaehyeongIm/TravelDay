package com.example.travelday_2

import TravelListAdapter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travelday_2.databinding.FragmentTraveladdBinding
import com.example.travelday_2.databinding.TravelListRowBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class TravelListFragment : Fragment() {
    lateinit var binding:FragmentTraveladdBinding
    lateinit var adapter: TravelListAdapter
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val currentDate: Date = Date()
    lateinit var re:String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= FragmentTraveladdBinding.inflate(layoutInflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initButton()
    }


    private fun calculateDday(date: Date): Int {
        val currentDate = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.time
        val diff = date.time - currentDate.time
        return (diff / (24 * 60 * 60 * 1000)).toInt()
    }




    private fun initButton() {
        binding.addTripButton.setOnClickListener {
            val countryFragment=CountryFragment()
            parentFragmentManager.beginTransaction().apply{
                add(R.id.frag_container,countryFragment)
                hide(this@TravelListFragment)
                commit()
            }
        }
    }

    private fun initRecyclerView() {
        adapter = TravelListAdapter(arrayListOf())
        binding.recyclerView.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL, false
        )
        binding.recyclerView.adapter = adapter
        if (userId != null) {
            DBRef.userRef.child(userId).addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val countryList = ArrayList<String>()
                    for (countrySnapshot in snapshot.children) {
                        countryList.add(countrySnapshot.key.toString())
                        Date()

                        run breaker@{
                            var d = countrySnapshot.children.forEach { dateSnapshot ->
                                val date = dateSnapshot.key
                                var d = date!!.split("-")
                                Log.i("디데이",Date(
                                    d[0].toInt(),
                                    d[1].toInt(),
                                    d[2].toInt()
                                ).toString())
                                re =  calculateDday(
                                    Date(
                                        d[0].toInt(),
                                        d[1].toInt(),
                                        d[2].toInt()
                                    )
                                ).toString()
                                Log.i(
                                    "디데이",
                                    calculateDday(
                                        Date(
                                            d[0].toInt(),
                                            d[1].toInt(),
                                            d[2].toInt()
                                        )
                                    ).toString()
                                )

                            return@breaker
                            }
                        }

//                        Date(d[0].toInt(),d[1].toInt(),d[2].toInt())
//                        Log.i("디데이",calculateDday(Date(d[0].toInt(),d[1].toInt(),d[2].toInt())).toString())
                    }
                    adapter.setData(countryList)

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("Firebase", "Failed to read value.", error.toException())
                }
            })
        }



        adapter.itemClickListener = object : TravelListAdapter.OnItemClickListener {
            override fun OnItemClick(data: String) {
                val bundle = Bundle().apply {
                    putString("클릭된 국가", data)
                }
                val dateListFragment = DateListFragment().apply {
                    arguments = bundle
                }
                parentFragmentManager.beginTransaction().apply {

                    add(R.id.frag_container, dateListFragment)
                    hide(this@TravelListFragment)
                    addToBackStack(null)
                    commit()
                }
            }}
        //remove, move 기능 구현
        val simpleCallback=object: ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT){
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
        val itemTouchHelper= ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }
    private fun convertStringToDate(dateString: String): Date {
        val format = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        return format.parse(dateString) ?: Date()
    }

    private fun getDatesBetween(startDate: Date, endDate: Date): java.util.ArrayList<String> {
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