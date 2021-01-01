package brotifypacha.scheduler.changes_list_fragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import brotifypacha.scheduler.R
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
        (binding.changesRecycler.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = true
        adapter = ChangeListAdapter(object : ChangeListAdapter.OnItemInteractListener {
            override fun onItemClick(position: Int) {
                adapter.setExpanded(position)
            }
            override fun onItemLongClick(position: Int): Boolean {
                return true
            }
        })
        binding.changesRecycler.adapter = adapter
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val scheduleId = requireArguments().getString(ARG_SCHEDULE_ID)
        viewModel = ViewModelProvider(this, ChangesListViewModel.Factory(scheduleId, requireActivity().application)).get(ChangesListViewModel::class.java)

        viewModel.getChangesLiveData().observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "triggering")
            adapter.setChanges(it)
        })
    }

}