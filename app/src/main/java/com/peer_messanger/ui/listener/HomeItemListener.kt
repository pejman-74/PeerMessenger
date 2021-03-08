package com.peer_messanger.ui.listener

import com.peer_messanger.data.model.Device

interface HomeItemListener {
    fun onClick(device: Device)
}