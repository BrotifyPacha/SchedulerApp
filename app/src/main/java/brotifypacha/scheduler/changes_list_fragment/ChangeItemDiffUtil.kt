package brotifypacha.scheduler.newList_list_fragment

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import brotifypacha.scheduler.Utils
import brotifypacha.scheduler.changes_list_fragment.ChangeListAdapter

class ChangeItemDiffUtil( ) {

    class Callback(val oldList: ArrayList<ChangeListAdapter.ChangeItem>,
                   val newList: ArrayList<ChangeListAdapter.ChangeItem>): DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return Utils.isSameDate(oldList[oldItemPosition].change.date, newList[newItemPosition].change.date)
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].expanded == newList[newItemPosition].expanded
        }
    }

    class ListUpdateCallback(val adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>): androidx.recyclerview.widget.ListUpdateCallback{
        override fun onInserted(position: Int, count: Int) {
            adapter.notifyItemRangeInserted(position+1, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            adapter.notifyItemRangeRemoved(position+1, count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            adapter.notifyItemMoved(fromPosition+1, toPosition+1)
        }

        override fun onChanged(position: Int, count: Int, payload: Any?) {
            adapter.notifyItemRangeChanged(position+1, count, payload)
        }

    }

}