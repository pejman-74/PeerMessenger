package com.peer_messanger.ui.adapters

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.peer_messanger.databinding.AvailableDevicesBinding
import com.peer_messanger.ui.holders.FindingItemViewHolder
import com.peer_messanger.ui.listener.BluetoothDeviceItemListener

class FindingRecyclerViewAdapter(private val bluetoothDeviceItemListener: BluetoothDeviceItemListener) :
    RecyclerView.Adapter<FindingItemViewHolder>() {

    //used for improve recycler view performance
    private val bluetoothDeviceDiffUtilItemCallback = object :
        DiffUtil.ItemCallback<BluetoothDevice>() {
        override fun areItemsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
            return oldItem.address == newItem.address
        }

        override fun areContentsTheSame(
            oldItem: BluetoothDevice,
            newItem: BluetoothDevice
        ): Boolean {
            return oldItem.name == oldItem.name
        }
    }
    private val differ = AsyncListDiffer(this, bluetoothDeviceDiffUtilItemCallback)


    fun setData(items: ArrayList<BluetoothDevice>) = differ.submitList(items)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FindingItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = AvailableDevicesBinding.inflate(layoutInflater, parent, false)
        return FindingItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FindingItemViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.bind(item)
        holder.itemView.setOnClickListener { bluetoothDeviceItemListener.onClick(item) }
    }

    override fun getItemCount(): Int = differ.currentList.size

}