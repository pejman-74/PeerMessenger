package com.peer_messanger.ui.holders

import androidx.recyclerview.widget.RecyclerView
import com.peer_messanger.databinding.PairedDeviceItemBinding
import com.peer_messanger.ui.adapters.HomeRecyclerViewItem

class PairedDevicesItemViewHolder(private val pairedDeviceItemBinding: PairedDeviceItemBinding) :
    RecyclerView.ViewHolder(pairedDeviceItemBinding.root) {


    fun bind(homeRecyclerViewItem: HomeRecyclerViewItem) {
        pairedDeviceItemBinding.aimPairedDevice.text = homeRecyclerViewItem.device.name
        pairedDeviceItemBinding.tvNamePairedDevice.text = homeRecyclerViewItem.device.name
        pairedDeviceItemBinding.tvLastMessagePairedDevice.text = homeRecyclerViewItem.lastMessage
    }
}