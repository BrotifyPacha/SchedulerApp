package brotifypacha.scheduler.changes_list_fragment

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import brotifypacha.scheduler.R
import brotifypacha.scheduler.Utils
import brotifypacha.scheduler.database.Change
import brotifypacha.scheduler.databinding.ChangeListItemBinding
import brotifypacha.scheduler.databinding.HeaderBinding
import brotifypacha.scheduler.databinding.LessonItemBinding
import java.lang.IllegalArgumentException

class ChangeListAdapter(private val listener: OnItemInteractListener): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ChangeItem(var expanded: Boolean=false, val change: Change)

    private val TAG: String = ChangeListAdapter::class.java.simpleName

    val dataSet: MutableList<ChangeItem> = ArrayList()
    private val HEADER_VIEWTYPE = 0
    private val ITEM_VIEWTYPE = 1

    interface OnItemInteractListener {
        fun onItemClick(position: Int) = Unit
        fun onItemLongClick(position: Int) : Boolean = false
    }

    fun setChanges(changes: ArrayList<Change>) {
        dataSet.clear()
        //TODO("implement diff util")
        changes.forEachIndexed{ i, it -> dataSet.add(i, ChangeItem(change = it)) }
        notifyDataSetChanged()
    }

    fun setExpanded(position: Int){
        dataSet[position].expanded = !dataSet[position].expanded
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HEADER_VIEWTYPE -> HeaderViewHolder.from(parent)
            ITEM_VIEWTYPE -> ChangeViewHolder.from(parent, listener)
            else -> throw IllegalArgumentException("Unknown ViewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)){
            ITEM_VIEWTYPE -> (holder as ChangeViewHolder).bind(dataSet[position-1])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> HEADER_VIEWTYPE
            else -> ITEM_VIEWTYPE
        }
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "${dataSet.size+1}")
        return dataSet.size + 1 // +1 slot for Header
    }

    class HeaderViewHolder(binding: HeaderBinding): RecyclerView.ViewHolder(binding.root){
        companion object{
            fun from(parent: ViewGroup): HeaderViewHolder {
                val inflater: LayoutInflater = LayoutInflater.from(parent.context)
                val binding: HeaderBinding = DataBindingUtil.inflate(inflater, R.layout.header, parent, false)
                binding.title = "Мои расписания"
                return HeaderViewHolder(binding)
            }
        }
    }

    class ChangeViewHolder(val binding: ChangeListItemBinding, listener: OnItemInteractListener): RecyclerView.ViewHolder(binding.root){

        init {
            binding.root.setOnClickListener{ listener.onItemClick(adapterPosition-1) }
            binding.root.setOnLongClickListener { listener.onItemLongClick(adapterPosition-1) }
        }

        companion object{
            fun from(parent: ViewGroup, listener: OnItemInteractListener): ChangeViewHolder {
                val inflater: LayoutInflater = LayoutInflater.from(parent.context)
                val binding: ChangeListItemBinding = DataBindingUtil.inflate(inflater, R.layout.change_list_item, parent, false)
                for (i in 1..9) {
                    val lessonBinding: LessonItemBinding = DataBindingUtil.inflate(inflater, R.layout.lesson_item, parent, false)
                    binding.listView.addView(lessonBinding.root)
                }
                return ChangeViewHolder(binding, listener)
            }
        }

        fun bind(changeItem: ChangeItem) {
            binding.date = Utils.formatDate(changeItem.change.date)
            binding.listView.visibility = if (changeItem.expanded) View.VISIBLE else View.GONE
        }
    }

}