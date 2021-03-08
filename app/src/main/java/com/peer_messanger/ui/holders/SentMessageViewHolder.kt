package com.peer_messanger.ui.holders

import androidx.recyclerview.widget.RecyclerView
import com.peer_messanger.R
import com.peer_messanger.data.model.BluetoothMessage
import com.peer_messanger.databinding.SentMessageBinding
import com.peer_messanger.util.setTailLength
import com.peer_messanger.util.toLocalTime

class SentMessageViewHolder(private val itemBinding: SentMessageBinding) :
    RecyclerView.ViewHolder(itemBinding.root) {
    fun bind(bluetoothMessage: BluetoothMessage) {
        itemBinding.tvChatMessage.text = bluetoothMessage.body
        itemBinding.tvChatTime.text = bluetoothMessage.createdTime?.toLocalTime()
        itemBinding.cvChatMessage.setTailLength("right", 10f)
        itemBinding.lavMessageStatus.apply {
            if (bluetoothMessage.isDelivered == true) {
                setImageResource(R.drawable.ic_check)
            } else {
                setAnimation(R.raw.message_watch)
            }
        }
    }
}