package com.peer_messanger.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.peer_messanger.R
import com.peer_messanger.data.wrapper.ConnectionEvents
import com.peer_messanger.databinding.FragmentChatBinding
import com.peer_messanger.ui.activity.MainActivity
import com.peer_messanger.ui.adapters.ChatRecyclerViewAdapter
import com.peer_messanger.ui.base.BaseFragment
import com.peer_messanger.ui.vm.MainViewModel

class ChatFragment : BaseFragment<MainViewModel, FragmentChatBinding>() {

    private lateinit var mainActivity: MainActivity
    private lateinit var chatRecyclerViewAdapter: ChatRecyclerViewAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = requireActivity() as MainActivity

        mainActivity.supportActionBar?.title = vModel.lastConnectedDevice.name

        vBinding.btnSendMessage.isEnabled = false
        vBinding.tiMessage.doOnTextChanged { _, _, _, count ->
            vBinding.btnSendMessage.isEnabled = count > 0
        }
        vBinding.btnSendMessage.setOnClickListener {
            vModel.sendMessage(
                vBinding.tiMessage.text.toString(),
                vModel.lastConnectedDevice.address
            )
            vBinding.tiMessage.text?.clear()
        }
        chatRecyclerViewAdapter = ChatRecyclerViewAdapter()
        vBinding.rvChat.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            setHasFixedSize(true)
            adapter = chatRecyclerViewAdapter
        }
        vModel.deviceWithMessages.observe(viewLifecycleOwner, { deviceWithMessages ->
            val messages =
                deviceWithMessages.receivedBluetoothMessages.plus(deviceWithMessages.sentBluetoothMessages)
                    .sortedBy { it.createdTime }
            chatRecyclerViewAdapter.setData(messages)
            vBinding.rvChat.smoothScrollToPosition(if (messages.isNotEmpty()) messages.size - 1 else 0)
        })

        vModel.getDeviceWithMessages(vModel.lastConnectedDevice.address)

        vModel.connectionState.asLiveData().observe(viewLifecycleOwner, {
            if (it is ConnectionEvents.Connected)
                hideDisconnectBar()
            if (it is ConnectionEvents.Disconnect)
                showDisconnectBar()

        })

    }

    private fun showDisconnectBar() {
        vBinding.btnChatDisconnectBarConnect.text =
            getString(R.string.try_connect)
        vBinding.clChatUserDisconnectBar.visibility = View.VISIBLE
        vBinding.btnChatDisconnectBarConnect.setOnClickListener {
            vModel.connectToDevice(vModel.lastConnectedDevice, true)
        }
    }

    private fun hideDisconnectBar() {
        vBinding.clChatUserDisconnectBar.visibility = View.GONE

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        vBinding = FragmentChatBinding.inflate(layoutInflater, container, false)
        return vBinding.root
    }

    override fun getViewModel() = activityViewModels<MainViewModel>()

    override fun getViewBinding(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentChatBinding.inflate(layoutInflater, container, false)


}