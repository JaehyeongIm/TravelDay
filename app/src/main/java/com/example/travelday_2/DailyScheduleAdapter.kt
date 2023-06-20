import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.travelday_2.DBRef
import com.example.travelday_2.DailyItem
import com.example.travelday_2.databinding.DailyListRowBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class DailyScheduleAdapter(
    val dailyItems: ArrayList<DailyItem>
) : RecyclerView.Adapter<DailyScheduleAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(data: DailyItem)
    }

    var itemClickListener: OnItemClickListener? = null

    inner class ViewHolder(val binding: DailyListRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.scheduleTextView.setOnClickListener {
                itemClickListener?.onItemClick(dailyItems[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DailyListRowBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dailyItems[position]
        holder.binding.timeTextView.text = item.time
        holder.binding.scheduleTextView.text = item.task
        holder.binding.colorView.setBackgroundColor(Color.parseColor(item.color))
    }

    override fun getItemCount(): Int {
        return dailyItems.size
    }
    fun updateItems(newItems: ArrayList<DailyItem>) {
        dailyItems.clear()
        dailyItems.addAll(newItems.sortedBy { it.time })
        notifyDataSetChanged()
    }
}
