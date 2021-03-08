package com.peer_messanger.ui.holders

import androidx.recyclerview.widget.RecyclerView
import com.peer_messanger.data.model.BluetoothMessage
import com.peer_messanger.databinding.ReceivedMessageBinding
import com.peer_messanger.util.setTailLength
import com.peer_messanger.util.toLocalTime

class ReceivedMessageViewHolder(private val itemBinding: ReceivedMessageBinding) :
    RecyclerView.ViewHolder(itemBinding.root) {
    fun bind(bluetoothMessage: BluetoothMessage) {
        itemBinding.tvChatMessage.text = bluetoothMessage.body
        itemBinding.tvChatTime.text = bluetoothMessage.createdTime?.toLocalTime()
        itemBinding.cvChatMessage.setTailLength("left", 10f)
    }
}
