package brotifypacha.scheduler.changes_list_fragment

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.*
import brotifypacha.scheduler.R
import brotifypacha.scheduler.Utils
import brotifypacha.scheduler.database.Change
import brotifypacha.scheduler.databinding.ChangeListItemBinding
import brotifypacha.scheduler.databinding.HeaderBinding
import brotifypacha.scheduler.databinding.LessonItemBinding
import brotifypacha.scheduler.newList_list_fragment.ChangeItemDiffUtil
import java.lang.IllegalArgumentException

class ChangeListAdapter(private val listener: OnItemInteractListener): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ChangeItem(var expanded: Boolean=false, val change: Change)

    private val TAG: String = ChangeListAdapter::class.java.simpleName
    private val dataset: ArrayList<ChangeItem> = ArrayList()

    companion object {
        private const val VIEWTYPE_ITEM = 1
        private const val VIEWTYPE_HEADER = 0
    }

    interface OnItemInteractListener {
        fun onItemClick(position: Int) = Unit
        fun onItemLongClick(position: Int) : Boolean = false
    }

    fun setChanges(changes: ArrayList<Change>) {
        val newList = ArrayList<ChangeItem>()
        changes.forEach { it -> newList.add(ChangeItem(false, it)) }
        val diffResult = DiffUtil.calculateDiff(ChangeItemDiffUtil.Callback(dataset, newList), true)
        dataset.clear()
        changes.forEachIndexed{ i, it -> dataset.add(i, ChangeItem(change = it)) }
        diffResult.dispatchUpdatesTo(ChangeItemDiffUtil.ListUpdateCallback(this))
    }

    fun toggleExpanded(position: Int){
        dataset[position].expanded = !dataset[position].expanded
        notifyItemChanged(position+1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEWTYPE_HEADER -> HeaderViewHolder.from(parent)
            VIEWTYPE_ITEM -> ChangeViewHolder.from(parent, listener)
            else -> throw IllegalArgumentException("Unknown ViewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            VIEWTYPE_ITEM -> (holder as ChangeViewHolder).bind(dataset[position - 1], position==dataset.size)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEWTYPE_HEADER
            else -> VIEWTYPE_ITEM
        }
    }

    override fun getItemCount(): Int {
        return dataset.size + 1 // +1 slot for Header
    }

    class HeaderViewHolder(binding: HeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): HeaderViewHolder {
                val inflater: LayoutInflater = LayoutInflater.from(parent.context)
                val binding: HeaderBinding = DataBindingUtil.inflate(inflater, R.layout.header, parent, false)
                binding.title = "Изменения"
                return HeaderViewHolder(binding)
            }
        }
    }

    class ChangeViewHolder(
            val binding: ChangeListItemBinding,
            private val nameBindings: ArrayList<LessonItemBinding>,
            listener: OnItemInteractListener) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.card.setOnClickListener { listener.onItemClick(adapterPosition - 1) }
            binding.card.setOnLongClickListener { listener.onItemLongClick(adapterPosition - 1) }
        }

        companion object {
            fun from(parent: ViewGroup, listener: OnItemInteractListener): ChangeViewHolder {
                val inflater: LayoutInflater = LayoutInflater.from(parent.context)
                val binding: ChangeListItemBinding = DataBindingUtil.inflate(inflater, R.layout.change_list_item, parent, false)
                val nameBindings: ArrayList<LessonItemBinding> = ArrayList<LessonItemBinding>()
                for (i in 1..9) {
                    val lessonBinding: LessonItemBinding = DataBindingUtil.inflate(inflater, R.layout.lesson_item, parent, false)
                    lessonBinding.setNumber(i.toString())
                    lessonBinding.number.visibility = View.VISIBLE
                    nameBindings.add(lessonBinding)
                    binding.listView.addView(lessonBinding.root)
                }
                val divider = binding.listView[0]
                binding.listView.removeViewAt(0)
                binding.listView.addView(divider)
                return ChangeViewHolder(binding, nameBindings, listener)
            }
        }

        fun bind(changeItem: ChangeItem, isLast: Boolean) {
            binding.date = Utils.formatDate(changeItem.change.date)
            if (changeItem.expanded) {
                binding.listView.visibility = View.VISIBLE
                //it item is last then change it's inner shadow to inner_shadow_top because visually
                //there's nothing at the bottom to cast shadow
                if (isLast) binding.listView.setBackgroundResource(R.drawable.inner_shadow_top)
                else binding.listView.setBackgroundResource(R.drawable.inner_shadow_vertical)
            } else {
                binding.listView.visibility = View.GONE
            }
            var foundNoneBlankItem = false
            for (i in 8 downTo 0) {
                nameBindings[i].root.visibility = View.VISIBLE
                nameBindings[i].divider.visibility = View.VISIBLE
                if (changeItem.change.change[i].isBlank() && !foundNoneBlankItem) {
                    nameBindings[i].root.visibility = View.GONE
                } else {
                    if (!foundNoneBlankItem) {
                        foundNoneBlankItem = true
                        nameBindings[i].divider.visibility = View.INVISIBLE
                    }
                    nameBindings[i].setName(changeItem.change.change[i])
                }
            }
        }
    }

    class ChangeItemAnimator : DefaultItemAnimator() {

        override fun animateChange(oldHolder: RecyclerView.ViewHolder?, newHolder: RecyclerView.ViewHolder?, fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
            if (newHolder == null) {
                dispatchChangeFinished(oldHolder, true)
                dispatchChangeFinished(newHolder, false)
                dispatchAnimationsFinished()
                return false
            }
            if (newHolder !is ChangeViewHolder || oldHolder !is ChangeViewHolder) {
                dispatchChangeFinished(oldHolder, true)
                dispatchChangeFinished(newHolder, false)
                dispatchAnimationsFinished()
                return false
            }
            val oldList = oldHolder.binding.listView
            val newList = newHolder.binding.listView
            if (oldList.visibility == newList.visibility) {
                dispatchChangeFinished(oldHolder, true)
                dispatchChangeFinished(newHolder, false)
                dispatchAnimationsFinished()
                return false
            }
            if (oldList.visibility == View.GONE) {
                //slide down (expand)
                newList.translationY = 0f - newList.height
                newList.alpha = 0f
                newList.animate()
                        .translationY(0f)
                        .alpha(1f)
                        .withEndAction {
                            newList.translationY = 0f
                            newList.alpha = 1f
                            dispatchChangeFinished(oldHolder, true)
                            dispatchChangeFinished(newHolder, false)
                            dispatchAnimationsFinished()
                        }
                        .setDuration(moveDuration)
                        .start()
            } else {
                //slide up (collapse)
                oldList.translationY = 0f
                oldList.alpha = 1f
                oldList.animate()
                        .translationY(0f - oldList.height)
                        .alpha(0f)
                        .withEndAction {
                            oldList.translationY = 0f
                            oldList.alpha = 1f
                            dispatchChangeFinished(oldHolder, true)
                            dispatchChangeFinished(newHolder, false)
                            dispatchAnimationsFinished()
                        }
                        .setDuration(moveDuration)
                        .start()
            }
            // animating viewHolder Y position if it was changed due to scrolling
            if (fromY - toY != 0) {
                oldHolder.itemView.animate().translationY(-(fromY-toY).toFloat()).withEndAction {
                    oldHolder.itemView.translationY = 0f
                    dispatchChangeFinished(oldHolder, true)
                    dispatchAnimationsFinished()
                }.start()
                newHolder.itemView.translationY = (fromY-toY).toFloat()
                newHolder.itemView.animate().translationY(0f).withEndAction {
                    dispatchChangeFinished(newHolder, false)
                }.start()
            }
            return true
        }

        override fun animateRemove(holder: RecyclerView.ViewHolder?): Boolean {
//            if (holder !is ChangeViewHolder) return super.animateRemove(holder)
//            holder.binding.card.animate().translationX((-holder.binding.card.width).toFloat()).start()
            dispatchRemoveFinished(holder)
            return true
        }

        override fun animateAdd(holder: RecyclerView.ViewHolder?): Boolean {
            return super.animateAdd(holder)
        }

        override fun animateMove(holder: RecyclerView.ViewHolder?, fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
            return super.animateMove(holder, fromX, fromY, toX, toY)
        }
    }

}