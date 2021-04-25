package com.peer_messanger.ui.holders

import androidx.recyclerview.widget.RecyclerView
import com.peer_messanger.R
import com.peer_messanger.data.model.Device
import com.peer_messanger.databinding.AvailableDevicesBinding

class FindingItemViewHolder(private val availableDevicesBinding: AvailableDevicesBinding) :
    RecyclerView.ViewHolder(availableDevicesBinding.root) {

    fun bind(device: Device) {
        availableDevicesBinding.aimAvailableDevice.apply {
            if (device.isSmartPhone)
                setImageResource(R.drawable.ic_smartphone)
            else
                setImageResource(R.drawable.ic_bluetooth)

        }
        availableDevicesBinding.tvNameAvailableDevice.text = device.name
        availableDevicesBinding.tvMacAddressAvailableDevice.text = device.macAddress
    }
}