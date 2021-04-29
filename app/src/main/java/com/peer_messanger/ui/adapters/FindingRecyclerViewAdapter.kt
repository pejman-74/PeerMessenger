package com.peer_messanger.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.peer_messanger.data.model.Device
import com.peer_messanger.databinding.AvailableDevicesBinding
import com.peer_messanger.ui.holders.FindingItemViewHolder
import com.peer_messanger.ui.listener.BluetoothDeviceItemListener

class FindingRecyclerViewAdapter(private val bluetoothDeviceItemListener: BluetoothDeviceItemListener) :
    ListAdapter<Device, FindingItemViewHolder>(bluetoothDeviceDiffUtilItemCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FindingItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = AvailableDevicesBinding.inflate(layoutInflater, parent, false)
        return FindingItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FindingItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener { bluetoothDeviceItemListener.onClick(item) }
    }


}

private val bluetoothDeviceDiffUtilItemCallback = object :
    DiffUtil.ItemCallback<Device>() {
    override fun areItemsTheSame(oldItem: Device, newItem: Device): Boolean {
        return oldItem.macAddress == newItem.macAddress
    }

    override fun areContentsTheSame(oldItem: Device, newItem: Device): Boolean {
        return oldItem == oldItem
    }
}