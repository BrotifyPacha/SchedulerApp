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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType){
            VIEW_TYPE_HEADER -> return HeaderViewHolder.from(parent)
            VIEW_TYPE_ITEM -> return ScheduleViewHolder.from(parent)
            VIEW_TYPE_PLUG -> return PlugViewHolder.from(parent)
            else -> throw IllegalArgumentException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)){
            // (position - 2) т.к. первый элемент это заголовок, следовательно все остальные смещены на два
            VIEW_TYPE_ITEM -> (holder as ScheduleViewHolder).bind(itemClickListener, itemLongClickListener, dataset.get(position - 2))
            VIEW_TYPE_PLUG -> (holder as PlugViewHolder).bind(dataset.isEmpty())
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) return VIEW_TYPE_HEADER
        if (position == 1) return VIEW_TYPE_PLUG
        return VIEW_TYPE_ITEM
    }

    override fun getItemCount(): Int {
        return dataset.size + 2 // Добавляется место для заголовка
    }

    fun setSchedules(schedules: List<Schedule>){
        dataset.clear()
        TODO("Заимплементить DiffUtil")
        /*schedules.forEach {
            dataset.add(it.copy())
        }*/
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

        fun bind(visible: Boolean){
            if (visible){
                v.setVisibility(View.VISIBLE)
                val tv = TypedValue()
                var appbarSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56f, parent.context.resources.displayMetrics).toInt()
                if (parent.context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
                {
                    appbarSize = TypedValue.complexToDimensionPixelSize(tv.data, parent.context.getResources().getDisplayMetrics())
                }
                v.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, parent.height - appbarSize)
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
            if (schedule.alias.length < 1){
                viewBinding.scheduleName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
                viewBinding.scheduleName.setPadding(
                    viewBinding.scheduleName.paddingLeft,
                    viewBinding.scheduleName.paddingTop,
                    viewBinding.scheduleName.paddingRight,
                    16
                )
                viewBinding.aliasLayout.visibility = View.GONE
            }
            viewBinding.schedule = schedule

            viewBinding.executePendingBindings()
        }
    }
}


