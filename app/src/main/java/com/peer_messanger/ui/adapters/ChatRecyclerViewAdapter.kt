package com.peer_messanger.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.peer_messanger.R
import com.peer_messanger.data.model.BluetoothMessage
import com.peer_messanger.databinding.ReceivedMessageBinding
import com.peer_messanger.databinding.SentMessageBinding
import com.peer_messanger.ui.holders.ReceivedMessageViewHolder
import com.peer_messanger.ui.holders.SentMessageViewHolder
import com.peer_messanger.util.selfUserDatabaseId
import java.util.*

class ChatRecyclerViewAdapter : ListAdapter<BluetoothMessage, RecyclerView.ViewHolder>(
    messageDiffUtilItemCallback
) {

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).messageOwner == selfUserDatabaseId)
            R.layout.sent_message
        else
            R.layout.received_message
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //set appropriate viewHolder by view type
        return when (viewType) {
            R.layout.received_message -> {
                val binding = ReceivedMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ReceivedMessageViewHolder(binding)
            }
            R.layout.sent_message -> {
                val binding =
                    SentMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SentMessageViewHolder(binding)
            }
            else -> {
                throw IllegalFormatFlagsException("Can't find appropriate viewHolder!")
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is SentMessageViewHolder -> {
                holder.bind(message)
            }
            is ReceivedMessageViewHolder -> {
                holder.bind(message)
            }
        }

    }

}

private val messageDiffUtilItemCallback = object :
    DiffUtil.ItemCallback<BluetoothMessage>() {
    override fun areItemsTheSame(
        oldItem: BluetoothMessage,
        newItem: BluetoothMessage
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: BluetoothMessage,
        newItem: BluetoothMessage
    ): Boolean {
        return oldItem == newItem
    }

}