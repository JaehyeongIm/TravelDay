import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travelday_2.DBRef
import com.example.travelday_2.DailyItem
import com.example.travelday_2.OutfitFragment
import com.example.travelday_2.R
import com.example.travelday_2.databinding.DateListRowBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.time.LocalDate
import java.time.format.DateTimeFormatterBuilder
import java.time.format.TextStyle
import java.time.temporal.ChronoField
import java.util.Locale

class DateListAdapter(
    private val context: Context,
    private val userId: String,
    private val country: String,
    val items: ArrayList<String>
) : RecyclerView.Adapter<DateListAdapter.ViewHolder>() {
    var dailyList = ArrayList<DailyItem>()

    interface OnItemClickListener {
        fun onItemClick(data: String)
        fun onOutfitClick(data: String)
    }

    var itemClickListener: OnItemClickListener? = null

    inner class ViewHolder(val binding: DateListRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.addButtonNew.setOnClickListener {
                itemClickListener?.onItemClick(items[adapterPosition])
            }

            binding.outfitBtn.setOnClickListener {
                val fragment = OutfitFragment()
                val fragmentManager = (binding.root.context as Fragment).parentFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.frag_container, fragment)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
            }
        }

        fun bind(date: String) {
            getDailyItems(userId, country, date)
        }
        private fun getDailyItems(userId: String, country: String, date: String) {
            val dailyItemsRef = DBRef.userRef.child(userId).child(country).child(date).child("tasklist")
            dailyItemsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val updatedDailyItems = ArrayList<DailyItem>()
                    snapshot.children.forEach { dailyItemSnapshot ->
                        val dailyItem = dailyItemSnapshot.getValue(DailyItem::class.java)
                        dailyItem?.let { updatedDailyItems.add(it) }
                    }
                    val adapter = DailyScheduleAdapter(ArrayList())
                    adapter.updateItems(updatedDailyItems)
                    binding.innerRecyclerview.adapter = adapter
                    binding.innerRecyclerview.layoutManager = LinearLayoutManager(context)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error here
                }
            })
        }
    }


    fun moveItem(oldPos: Int, newPos: Int) {
        val item = items[oldPos]
        items.removeAt(oldPos)
        items.add(newPos, item)
        notifyItemMoved(oldPos, newPos)
    }

    fun removeItem(pos: Int) {
        items.removeAt(pos)
        notifyItemRemoved(pos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DateListRowBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        // 문자열 날짜를 LocalDate로 변환
        val formatter = DateTimeFormatterBuilder()
            .appendPattern("YYYY-MM-dd")
            .parseDefaulting(ChronoField.YEAR, LocalDate.now().year.toLong())
            .toFormatter()
        val date = LocalDate.parse(item, formatter)

        // 요일 이름 얻기
        val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())

        // 요일 이름의 첫 글자만 추출
        val firstLetterOfDay = dayOfWeek[0]

        // 날짜와 요일을 출력
        holder.binding.dateTextView.text = "•${item} ($firstLetterOfDay)"
        holder.binding.outfitBtn.setOnClickListener{
            itemClickListener?.onOutfitClick(item)
        }
    }
}
