package com.peer_messanger.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.peer_messanger.R
import com.peer_messanger.data.wrapper.ConnectionEvents
import com.peer_messanger.databinding.FragmentChatBinding
import com.peer_messanger.ui.adapters.ChatRecyclerViewAdapter
import com.peer_messanger.ui.vm.MainViewModel
import com.peer_messanger.util.appCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ChatFragment : Fragment() {

    private val chatRecyclerViewAdapter by lazy { ChatRecyclerViewAdapter() }
    private val args: ChatFragmentArgs by navArgs()

    private var _vBinding: FragmentChatBinding? = null
    private val vBinding get() = _vBinding!!

    private val vModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().appCompatActivity().supportActionBar?.title =
            args.device.name

        setListeners()

        setRecyclerView()

        setObservers()


    }

    private fun setRecyclerView() {
        vBinding.rvChat.apply {
            adapter = chatRecyclerViewAdapter
        }
    }

    private fun setListeners() {
        vBinding.btnSendMessage.setOnClickListener {

            if (vBinding.tiMessage.text.isNullOrBlank())
                return@setOnClickListener

            vModel.sendMessage(
                vBinding.tiMessage.text.toString(),
                args.device.macAddress
            )

            vBinding.tiMessage.text?.clear()
        }
    }


    private fun setObservers() {

        lifecycleScope.launchWhenStarted {
            vModel.deviceWithMessages.collect { deviceWithMessages ->
                deviceWithMessages ?: return@collect
                val messages = deviceWithMessages.sortedMessages
                chatRecyclerViewAdapter.submitList(deviceWithMessages.sortedMessages)
                vBinding.rvChat.smoothScrollToPosition(if (messages.isNotEmpty()) messages.size - 1 else 0)
            }
        }
        lifecycleScope.launchWhenStarted {
            vModel.connectionState.collect {
                if (it is ConnectionEvents.Connected)
                    hideDisconnectBar()
                if (it is ConnectionEvents.Disconnect || it is ConnectionEvents.ConnectionFailed)
                    showDisconnectBar()
            }
        }

        vModel.getDeviceWithMessages(args.device.macAddress)
    }

    private fun showDisconnectBar() {
        vBinding.btnChatDisconnectBarConnect.text =
            getString(R.string.try_connect)
        vBinding.clChatUserDisconnectBar.visibility = View.VISIBLE
        vBinding.btnChatDisconnectBarConnect.setOnClickListener {
            vModel.connectToDevice(args.device.macAddress)
        }
    }

    private fun hideDisconnectBar() {
        vBinding.clChatUserDisconnectBar.visibility = View.GONE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _vBinding = FragmentChatBinding.inflate(layoutInflater, container, false)
        return vBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _vBinding = null
    }


}