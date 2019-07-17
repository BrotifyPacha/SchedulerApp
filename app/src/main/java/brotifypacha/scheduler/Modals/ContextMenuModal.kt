package brotifypacha.scheduler.Modals

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import brotifypacha.scheduler.R;
import brotifypacha.scheduler.databinding.ModalMenuItemBinding
import kotlinx.android.synthetic.main.fragment_modal_menu.*


/**
 *
 * A fragment that shows a list of items as a modal bottom sheet.
 *
 * You can show this modal bottom sheet from your activity like this:
 * <pre>
 *    ContextMenuModal.newInstance(30).show(supportFragmentManager, "dialog")
 * </pre>
 *
 * You activity (or fragment) needs to implement [ContextMenuModal.Listener].
 */
class ContextMenuModal : BottomSheetDialogFragment() {

    val TAG = ContextMenuModal::class.java.simpleName
    private var mListener: Listener? = null

    companion object {
        val FRAGMENT_TAG = "context_menu_modal"
        val ARG_ITEMS = "arg_items"
        fun newInstance(items: List<ModalMenuItem>): ContextMenuModal =
            ContextMenuModal().apply {
                arguments = Bundle().apply {
                    val arrayList = ArrayList(items)
                    putParcelableArrayList(ARG_ITEMS, arrayList)
                }
            }

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_modal_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list.layoutManager = LinearLayoutManager(context)
        list.adapter = ActionAdapter(
            arguments!!.getParcelableArrayList<ModalMenuItem>(ARG_ITEMS) as ArrayList<ModalMenuItem>, mListener
        )
    }

    override fun onDetach() {
        mListener = null
        super.onDetach()
    }

    class Listener(val listener: (scheduleId: Int) -> Unit) {
        fun onItemClick(id: Int) = listener(id)
    }

    fun setOnItemClickListener(listener: (position: Int) -> Unit){
        mListener = Listener(listener)
    }

    class ModalItemViewHolder(val binding: ModalMenuItemBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ModalItemViewHolder {
                val binding: ModalMenuItemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.modal_menu_item, parent, false)
                return ModalItemViewHolder(binding)
            }
        }

        fun bind(items: ArrayList<ModalMenuItem>, position: Int, clickListener: Listener?){
            binding.text.text = items[position].title
            binding.root.setOnClickListener {
                if (clickListener != null) {
                    clickListener.onItemClick(items[position].id)
                }
            }
            binding.icon.setImageResource(items[position].icon)
        }
    }

    class ActionAdapter(private val items: ArrayList<ModalMenuItem>, val clickListener: Listener?) :
        RecyclerView.Adapter<ModalItemViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModalItemViewHolder {
            return ModalItemViewHolder.from(parent)
        }

        override fun onBindViewHolder(holder: ModalItemViewHolder, position: Int) {
            holder.bind(items, position, clickListener)
        }

        override fun getItemCount(): Int {
            return items.size
        }
    }

    data class ModalMenuItem(val id: Int, val title: String, val icon: Int) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString()!!,
            parcel.readInt()
        ) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(id)
            parcel.writeString(title)
            parcel.writeInt(icon)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<ModalMenuItem> {
            override fun createFromParcel(parcel: Parcel): ModalMenuItem {
                return ModalMenuItem(parcel)
            }

            override fun newArray(size: Int): Array<ModalMenuItem?> {
                return arrayOfNulls(size)
            }
        }
    }


}
