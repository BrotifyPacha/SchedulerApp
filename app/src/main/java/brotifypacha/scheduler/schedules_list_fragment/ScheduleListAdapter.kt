package brotifypacha.scheduler.schedules_list_fragment

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import brotifypacha.scheduler.R
import brotifypacha.scheduler.database.Schedule
import brotifypacha.scheduler.databinding.HeaderBinding
import brotifypacha.scheduler.databinding.ScheduleListItemBinding
import java.lang.IllegalArgumentException

class ScheduleListAdapter(val itemClickListener: OnScheduleClickListener, val itemLongClickListener: OnScheduleLongClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val dataset: MutableList<Schedule> = ArrayList()
    val VIEW_TYPE_HEADER = 0
    val VIEW_TYPE_ITEM = 1
    val VIEW_TYPE_PLUG = 2
    val VIEW_TYPE_END_SPACING = 3

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType){
            VIEW_TYPE_HEADER -> return HeaderViewHolder.from(parent)
            VIEW_TYPE_ITEM -> return ScheduleViewHolder.from(parent)
            VIEW_TYPE_PLUG -> return PlugViewHolder.from(parent)
            VIEW_TYPE_END_SPACING -> return PlugViewHolder.from(parent)
            else -> throw IllegalArgumentException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)){
            // position-2 (0 - header, 1 - plugView which is hidden by default
            VIEW_TYPE_ITEM -> (holder as ScheduleViewHolder).bind(itemClickListener, itemLongClickListener, dataset.get(position - 2))
            VIEW_TYPE_PLUG -> (holder as PlugViewHolder).bind(dataset.isEmpty())
            VIEW_TYPE_END_SPACING -> (holder as PlugViewHolder).bindEndSpacing()
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) return VIEW_TYPE_HEADER
        if (position == 1) return VIEW_TYPE_PLUG
        if (position == dataset.size+2) return VIEW_TYPE_END_SPACING // if it's last item, then it's End Spacing
        return VIEW_TYPE_ITEM
    }

    override fun getItemCount(): Int {
        return dataset.size + 3 // +1 - header, +1 - plugView, +1 - end spacing
    }

    fun setSchedules(schedules: List<Schedule>){
        dataset.clear()
        //TODO("Заимплементить DiffUtil")
        //schedules.forEach {
        //    dataset.add(it.copy())
        //}
        dataset.addAll(schedules)
        notifyDataSetChanged()
    }

    class PlugViewHolder(val v: View, val parent: ViewGroup): RecyclerView.ViewHolder(v){
        companion object{
            fun from(parent: ViewGroup): PlugViewHolder{
                val v = LayoutInflater.from(parent.context).inflate(R.layout.schedule_list_plug, parent, false)
                return PlugViewHolder(v, parent)
            }
        }
        fun bindEndSpacing(){
            val tv = TypedValue()
            var appbarSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56f, parent.context.resources.displayMetrics).toInt()
            if (parent.context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                appbarSize = TypedValue.complexToDimensionPixelSize(tv.data, parent.context.getResources().getDisplayMetrics())
            }
            v.visibility = View.INVISIBLE
            v.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,  (appbarSize * 1.5).toInt())
        }

        fun bind(visible: Boolean){
            if (visible){
                v.setVisibility(View.VISIBLE)
                val tv = TypedValue()
                var appbarSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56f, parent.context.resources.displayMetrics).toInt()
                if (parent.context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
                {
                    appbarSize = TypedValue.complexToDimensionPixelSize(tv.data, parent.context.getResources().getDisplayMetrics())
                }
                val headerSize = parent.getChildAt(0).height ?: 0
                v.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, parent.height - headerSize - appbarSize)
            } else {
                v.setVisibility(View.GONE)
                v.layoutParams = RecyclerView.LayoutParams(0, 0)
            }
        }
    }

    class HeaderViewHolder(binding: HeaderBinding): RecyclerView.ViewHolder(binding.root){
        companion object{
            fun from(parent: ViewGroup): HeaderViewHolder{
                val inflater: LayoutInflater = LayoutInflater.from(parent.context)
                val binding: HeaderBinding = DataBindingUtil.inflate(inflater, R.layout.header, parent, false)
                binding.title = "Мои расписания"
                return HeaderViewHolder(binding)
            }
        }
    }

    class ScheduleViewHolder(var viewBinding: ScheduleListItemBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        companion object{
            fun from(parent: ViewGroup) : ScheduleViewHolder {
                val inflater: LayoutInflater = LayoutInflater.from(parent.context)
                val binding: ScheduleListItemBinding = DataBindingUtil.inflate(inflater, R.layout.schedule_list_item, parent, false)
                return ScheduleViewHolder(binding)
            }
        }

        fun bind(clickListener: OnScheduleClickListener, longClickListener: OnScheduleLongClickListener, schedule: Schedule){
            viewBinding.root.setOnClickListener {
                clickListener.onClick(schedule)
            }
            viewBinding.root.setOnLongClickListener {
                longClickListener.onClick(schedule)
            }
            viewBinding.schedule = schedule

            viewBinding.executePendingBindings()
        }
    }
}


