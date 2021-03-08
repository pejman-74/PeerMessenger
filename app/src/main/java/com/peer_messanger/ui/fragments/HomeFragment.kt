package com.peer_messanger.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.peer_messanger.data.model.Device
import com.peer_messanger.databinding.FragmentHomeBinding
import com.peer_messanger.ui.adapters.HomeRecyclerViewAdapter
import com.peer_messanger.ui.adapters.HomeRecyclerViewItem
import com.peer_messanger.ui.base.BaseFragment
import com.peer_messanger.ui.listener.HomeItemListener
import com.peer_messanger.ui.vm.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<MainViewModel, FragmentHomeBinding>(), HomeItemListener {

    lateinit var homeRecyclerViewAdapter: HomeRecyclerViewAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        vBinding.fabAddNew.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToFindingFragment())
        }

        homeRecyclerViewAdapter = HomeRecyclerViewAdapter(this)
        vBinding.rvHome.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = homeRecyclerViewAdapter
        }

        vModel.allDevicesWithMessages.asLiveData()
            .observe(viewLifecycleOwner, { listDeviceWithMessages ->
                val homeRecyclerViewItems = ArrayList<HomeRecyclerViewItem>()

                listDeviceWithMessages.forEach { deviceWithMessages ->
                    val lastMessage =
                        deviceWithMessages.receivedBluetoothMessages.plus(deviceWithMessages.sentBluetoothMessages)
                            .maxByOrNull { it.createdTime }
                    homeRecyclerViewItems.add(
                        HomeRecyclerViewItem(
                            deviceWithMessages.device,
                            lastMessage?.body
                        )
                    )
                }
                homeRecyclerViewAdapter.setData(homeRecyclerViewItems)
            })
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