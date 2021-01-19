package brotifypacha.scheduler.changes_list_fragment

import android.graphics.drawable.AnimationDrawable
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import brotifypacha.scheduler.AnimUtils
import brotifypacha.scheduler.ItemSwipeHelper
import brotifypacha.scheduler.R
import brotifypacha.scheduler.add_change_fragment.AddChangeFragment
import brotifypacha.scheduler.databinding.ChangesListFragmentBinding

class ChangesListFragment : Fragment() {
    private val TAG: String = ChangesListFragment::class.java.simpleName

    companion object {
        const val ARG_SCHEDULE_ID: String = "schedule_id"
        fun newInstance(scheduleId: String) = ChangesListFragment().apply {
            arguments = Bundle().apply { putString(ARG_SCHEDULE_ID, scheduleId) }
        }
    }

    private lateinit var viewModel: ChangesListViewModel
    private lateinit var binding: ChangesListFragmentBinding
    private lateinit var adapter: ChangeListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.changes_list_fragment, container, false)

        binding.changesRecycler.layoutManager = LinearLayoutManager(inflater.context)
        binding.changesRecycler.setChildDrawingOrderCallback {childCount, position -> childCount-position-1 }
        binding.changesRecycler.itemAnimator = ChangeListAdapter.ChangeItemAnimator()
        adapter = ChangeListAdapter(object : ChangeListAdapter.OnItemInteractListener {
            override fun onItemClick(position: Int) {
                adapter.toggleExpanded(position)
            }
            override fun onItemLongClick(position: Int): Boolean {
                return true
            }
        })
        binding.changesRecycler.adapter = adapter
        binding.changesRecycler.addOnItemTouchListener(ItemSwipeHelper(object: ItemSwipeHelper.OnSwipeListener{
            var pastThreshold: Boolean = false
            override fun getSwipeThreshold(): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 88f, resources.displayMetrics)
            override fun getSwipeDirection(): Int = ItemSwipeHelper.SWIPE_LEFT
            override fun onMove(holder: RecyclerView.ViewHolder, dx: Float) {
                if (holder !is ChangeListAdapter.ChangeViewHolder) return
                val card = holder.binding.card
                if (dx < 0) card.translationX = dx
                else card.translationX = 0f

                if (dx < -getSwipeThreshold() && !pastThreshold) {
                    pastThreshold = true
                    AnimUtils.animateViewWiggle(holder.binding.image, holder.binding.image.width.toFloat()/5)
                    holder.itemView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
                } else if (dx > -getSwipeThreshold() && pastThreshold){
                    pastThreshold =  false
                    holder.itemView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
                }
                if (dx < -2f) {
                    card.setBackgroundResource(R.drawable.animated_round_corners)
                    (card.background as AnimationDrawable).start()
                } else {
                    card.setBackgroundResource(R.drawable.reversed_round_corners)
                    (card.background as AnimationDrawable).start()
                }
            }

            override fun onUp(holder: RecyclerView.ViewHolder, isSwipeSuccessful: Boolean) {
                pastThreshold = false
                if (holder !is ChangeListAdapter.ChangeViewHolder) return
                val card = holder.binding.card
                if (isSwipeSuccessful) {
                    holder.binding.card.animate()
                            .translationX( holder.itemView.width * -1f)
                            .withEndAction {
                                viewModel.removeChange(holder.binding.date!!)
                                card.setBackgroundResource(R.drawable.reversed_round_corners)
                                (card.background as AnimationDrawable).start()
                            }
                } else {
                    // Duration of anim depends on translationX.
                    //(min duration = 50ms, max depends on translationX and getSwipeThreshold Ratio)
                    val duration = Math.abs(card.translationX) / getSwipeThreshold() * 50 + 200f
                    card.animate()
                            .translationX(0f)
                            .setDuration(duration.toLong())
                            .withEndAction {
                                card.setBackgroundResource(R.drawable.reversed_round_corners)
                                (card.background as AnimationDrawable).start()
                            }
                            .start()
                }
            }

        }))

        binding.addChangeButton.setOnClickListener {
            findNavController().navigate(R.id.action_add_change_from_view_changes, Bundle().apply {
                putString(AddChangeFragment.ARG_SCHEDULE_ID, requireArguments().getString(ARG_SCHEDULE_ID))
            })
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val scheduleId = requireArguments().getString(ARG_SCHEDULE_ID)
        viewModel = ViewModelProvider(this, ChangesListViewModel.Factory(scheduleId, requireActivity().application)).get(ChangesListViewModel::class.java)

        viewModel.getChangesLiveData().observe(viewLifecycleOwner, Observer {
            adapter.setChanges(it)
        })
    }

}