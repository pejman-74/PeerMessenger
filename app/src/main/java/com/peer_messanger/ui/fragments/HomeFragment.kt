package com.peer_messanger.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.peer_messanger.data.model.Device
import com.peer_messanger.databinding.FragmentHomeBinding
import com.peer_messanger.ui.adapters.HomeRecyclerViewAdapter
import com.peer_messanger.ui.adapters.HomeRecyclerViewItem
import com.peer_messanger.ui.base.BaseFragment
import com.peer_messanger.ui.listener.HomeItemListener
import com.peer_messanger.ui.vm.MainViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect

@ExperimentalCoroutinesApi
class HomeFragment : BaseFragment<MainViewModel, FragmentHomeBinding>(), HomeItemListener {

    private val homeRecyclerViewAdapter by lazy { HomeRecyclerViewAdapter(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListeners()
        setRecyclerView()
        setObservers()


    }

    private fun setObservers() {
        lifecycleScope.launchWhenStarted {
            vModel.allDevicesWithMessages.collect { listDeviceWithMessages ->
                val homeRecyclerViewItems = listDeviceWithMessages.map { deviceWithMessages ->

                    val lastMessage = deviceWithMessages.lastMessage?.body

                    HomeRecyclerViewItem(deviceWithMessages.device, lastMessage)
                }
                homeRecyclerViewAdapter.submitList(homeRecyclerViewItems)
            }
        }
    }

    private fun setListeners() {
        vBinding.fabAddNew.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToFindingFragment())
        }
    }

    private fun setRecyclerView() {
        vBinding.rvHome.apply {
            adapter = homeRecyclerViewAdapter
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        vBinding =
            FragmentHomeBinding.inflate(inflater, container, false)
        return vBinding.root
    }

    override fun onClick(device: Device) {
        findNavController().navigate(HomeFragmentDirections.actionGlobalChatFragment(device))
    }

    override fun getViewModel() = activityViewModels<MainViewModel>()


    override fun getViewBinding(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentHomeBinding.inflate(layoutInflater, container, false)


}