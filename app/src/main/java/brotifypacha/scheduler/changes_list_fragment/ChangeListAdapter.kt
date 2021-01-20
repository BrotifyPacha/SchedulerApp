package brotifypacha.scheduler.changes_list_fragment

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.addListener
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.*
import brotifypacha.scheduler.R
import brotifypacha.scheduler.Utils
import brotifypacha.scheduler.database.Change
import brotifypacha.scheduler.databinding.ChangeListItemBinding
import brotifypacha.scheduler.databinding.HeaderBinding
import brotifypacha.scheduler.databinding.LessonItemBinding
import brotifypacha.scheduler.databinding.PlugListItemBinding
import brotifypacha.scheduler.newList_list_fragment.ChangeItemDiffUtil
import kotlinx.android.synthetic.main.lesson_item.view.*
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
            private val plugViewBinding: PlugListItemBinding,
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
                val plugListItemBinding: PlugListItemBinding = DataBindingUtil.inflate(inflater, R.layout.plug_list_item, parent, false)
                plugListItemBinding.message = "Занятий нет"
                plugListItemBinding.divider.visibility = View.INVISIBLE
                binding.listView.addView(plugListItemBinding.root)

                val divider = binding.listView[0]
                binding.listView.removeViewAt(0)
                binding.listView.addView(divider)
                return ChangeViewHolder(binding, nameBindings, plugListItemBinding, listener)
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
                var foundNotBlankItem = false
                for (i in 8 downTo 0) {
                    //TODO (" Переписать тело цикла, может быть do while? ")
                    nameBindings[i].root.visibility = View.VISIBLE
                    nameBindings[i].divider.visibility = View.VISIBLE
                    if (changeItem.change.change[i].isBlank() && !foundNotBlankItem) {
                        nameBindings[i].root.visibility = View.GONE
                    } else {
                        if (!foundNotBlankItem) {
                            foundNotBlankItem = true
                            nameBindings[i].divider.visibility = View.INVISIBLE
                        }
                        nameBindings[i].setName(changeItem.change.change[i])
                    }
                }
                if (!foundNotBlankItem){
                    plugViewBinding.root.visibility = View.VISIBLE
                    binding.listView.divider.visibility = View.GONE
                } else {
                    plugViewBinding.root.visibility = View.GONE
                    binding.listView.divider.visibility = View.VISIBLE
                }
            } else {
                binding.listView.visibility = View.GONE
            }
        }
    }

    class ChangeItemAnimator : DefaultItemAnimator() {

        var running = false

        override fun animateChange(oldHolder: RecyclerView.ViewHolder?, newHolder: RecyclerView.ViewHolder?, fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
            if (newHolder == null || newHolder !is ChangeViewHolder || oldHolder !is ChangeViewHolder) {
                return false
            }

            val oldList = oldHolder.binding.listView
            val newList = newHolder.binding.listView
            if (oldList.visibility == newList.visibility) {
                return false
            }
            val animList = ArrayList<Animator>()
            if (oldList.visibility == View.GONE) {
                //slide down (expand)
                val translationY = ObjectAnimator.ofFloat(newList, View.TRANSLATION_Y, newList.height * -1f, 0f)
                val alpha = ObjectAnimator.ofFloat(newList, View.ALPHA, 0f, 1f)
                animList.add(translationY)
                animList.add(alpha)
            } else {
                //slide up (collapse)
                val translationY = ObjectAnimator.ofFloat(oldList, View.TRANSLATION_Y, 0f, oldList.height * -1f)
                val alpha = ObjectAnimator.ofFloat(oldList, View.ALPHA, 1f, 0f)
                animList.add(translationY)
                animList.add(alpha)
            }
            // animating viewHolder Y position if it was changed due to scrolling
            val oldViewTranslation = ObjectAnimator.ofFloat(oldHolder.itemView, View.TRANSLATION_Y, (toY-fromY).toFloat())
            val newViewTranslation = ObjectAnimator.ofFloat(newHolder.itemView, View.TRANSLATION_Y, (fromY-toY).toFloat(), 0f)
            // animating new holder alpha to appear smoothly (Otherwise new holder would blink upon first click)
            val newViewAlpha = ObjectAnimator.ofFloat(newHolder.itemView, View.ALPHA, 0f, 1f)
            animList.add(oldViewTranslation)
            animList.add(newViewTranslation)
            animList.add(newViewAlpha)
            val set = AnimatorSet()
            set.playTogether(animList)
            set.addListener( onStart = {
                running = true
                dispatchChangeStarting(oldHolder, true)
                dispatchChangeStarting(newHolder, false)
                dispatchAnimationStarted(oldHolder)
                dispatchAnimationStarted(newHolder)
            }, onEnd = {
                running = false
                newList.alpha = 1f
                oldList.alpha = 1f
                newList.translationY = 0f
                oldList.translationY = 0f
                oldHolder.itemView.translationY = 0f
                newHolder.itemView.translationY = 0f
                dispatchChangeFinished(oldHolder, true)
                dispatchChangeFinished(newHolder, false)
                dispatchAnimationsFinished()
            })
            set.start()
            return false
        }

        override fun isRunning(): Boolean {
            return if (!running) super.isRunning()
            else true
        }

        override fun animateRemove(holder: RecyclerView.ViewHolder?): Boolean {
            if (holder !is ChangeViewHolder) return false
            dispatchRemoveStarting(holder)
            val animList = ArrayList<Animator>()
            if (holder.binding.listView.visibility != View.GONE){
                val listTranslationY = ObjectAnimator.ofFloat(holder.binding.listView, View.TRANSLATION_Y, holder.itemView.height * -1f)
                animList.add(listTranslationY)
            }
            val cardAlpha = ObjectAnimator.ofFloat(holder.itemView, View.ALPHA, 0f)
            animList.add(cardAlpha)
            val set = AnimatorSet()
            set.addListener( onEnd = {
                holder.itemView.alpha = 1f
                holder.binding.listView.translationY = 0f
                holder.binding.card.translationX = 0f
                dispatchRemoveFinished(holder)
                dispatchAnimationsFinished()
            } )
            set.playTogether(animList)
            set.start()
            return false
        }
    }

}