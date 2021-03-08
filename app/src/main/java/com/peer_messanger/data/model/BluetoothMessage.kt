package com.peer_messanger.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity
data class BluetoothMessage(
    @SerializedName("id")
    @PrimaryKey(autoGenerate = false)
    val id: String,
    @SerializedName("body")
    val body: String,
    @SerializedName("messageOwner")
    @Expose(serialize = false,deserialize = false)
    val messageOwner: String,
    @SerializedName("receiverDevice")
    @Expose(serialize = false,deserialize = false)
    val receiverDevice: String,
    @SerializedName("createdTime")
    @Expose(serialize = false,deserialize = false)
    val createdTime: String,
    @SerializedName("isDelivered")
    @Expose(serialize = false,deserialize = false)
    val isDelivered: Boolean? = null
)