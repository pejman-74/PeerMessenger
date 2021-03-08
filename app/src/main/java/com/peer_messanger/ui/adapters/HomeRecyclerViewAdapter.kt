package com.peer_messanger.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.peer_messanger.data.model.Device
import com.peer_messanger.databinding.PairedDeviceItemBinding
import com.peer_messanger.ui.holders.PairedDevicesItemViewHolder
import com.peer_messanger.ui.listener.HomeItemListener

class HomeRecyclerViewAdapter(private val homeItemListener: HomeItemListener) :
    RecyclerView.Adapter<PairedDevicesItemViewHolder>() {

    //used for improve recycler view performance
    private val homeRecyclerViewItemDiffUtilItemCallback = object :
        DiffUtil.ItemCallback<HomeRecyclerViewItem>() {
        override fun areItemsTheSame(
            oldItem: HomeRecyclerViewItem,
            newItem: HomeRecyclerViewItem
        ): Boolean {
            return oldItem.device.macAddress == newItem.device.macAddress
        }

        override fun areContentsTheSame(
            oldItem: HomeRecyclerViewItem,
            newItem: HomeRecyclerViewItem
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }
    private val differ = AsyncListDiffer(this, homeRecyclerViewItemDiffUtilItemCallback)

    fun setData(items: List<HomeRecyclerViewItem>) =
        differ.submitList(items)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PairedDevicesItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = PairedDeviceItemBinding.inflate(layoutInflater, parent, false)
        return PairedDevicesItemViewHolder(binding)

    }

    override fun onBindViewHolder(holder: PairedDevicesItemViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.bind(item)
        holder.itemView.setOnClickListener { homeItemListener.onClick(item.device) }
    }

    override fun getItemCount(): Int = differ.currentList.size


}

data class HomeRecyclerViewItem(val device: Device, val lastMessage: String?)
