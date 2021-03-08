package com.peer_messanger.ui.holders

import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import androidx.recyclerview.widget.RecyclerView
import com.peer_messanger.R
import com.peer_messanger.databinding.AvailableDevicesBinding

class FindingItemViewHolder(private val availableDevicesBinding: AvailableDevicesBinding) :
    RecyclerView.ViewHolder(availableDevicesBinding.root) {

    fun bind(bluetoothDevice: BluetoothDevice) {
        availableDevicesBinding.aimAvailableDevice.apply {
            when (bluetoothDevice.bluetoothClass.deviceClass) {
                BluetoothClass.Device.PHONE_SMART -> setImageResource(R.drawable.ic_smartphone)
                else -> setImageResource(R.drawable.ic_bluetooth)
            }
        }
        availableDevicesBinding.tvNameAvailableDevice.text = bluetoothDevice.name
        availableDevicesBinding.tvMacAddressAvailableDevice.text = bluetoothDevice.address
    }
}