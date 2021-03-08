package com.peer_messanger.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.peer_messanger.R
import com.peer_messanger.data.wrap.BluetoothEventResource
import com.peer_messanger.databinding.FragmentChatBinding
import com.peer_messanger.ui.activity.MainActivity
import com.peer_messanger.ui.adapters.ChatRecyclerViewAdapter
import com.peer_messanger.ui.base.BaseFragment
import com.peer_messanger.ui.vm.MainViewModel

class ChatFragment : BaseFragment<MainViewModel, FragmentChatBinding>() {

    private lateinit var mainActivity: MainActivity
    private lateinit var chatRecyclerViewAdapter: ChatRecyclerViewAdapter
    private val args: ChatFragmentArgs by navArgs()
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainActivity = requireActivity() as MainActivity

        mainActivity.supportActionBar?.title = args.device.name

        vBinding.btnSendMessage.isEnabled = false
        vBinding.tiMessage.doOnTextChanged { _, _, _, count ->
            vBinding.btnSendMessage.isEnabled = count > 0
        }
        vBinding.btnSendMessage.setOnClickListener {
            mainActivity.sendMessage(vBinding.tiMessage.text.toString(), args.device.macAddress)
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

        vModel.getDeviceWithMessages(args.device.macAddress)

        vModel.bluetoothEventsEventResource.observe(viewLifecycleOwner, {
            when (it) {
                is BluetoothEventResource.DeviceConnected -> hideDisconnectBar()

                is BluetoothEventResource.DeviceDisconnected -> showDisconnectBar()

                BluetoothEventResource.DeviceConnecting -> {
                    vBinding.btnChatDisconnectBarConnect.text =
                        getString(R.string.connecting)
                }
            }
        })

    }

    private fun showDisconnectBar() {
        vBinding.btnChatDisconnectBarConnect.text =
            getString(R.string.try_connect)
        vBinding.clChatUserDisconnectBar.visibility = View.VISIBLE
        vBinding.btnChatDisconnectBarConnect.setOnClickListener {
            mainActivity.connectToDevice(args.device.macAddress)
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