package com.peer_messanger.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity
data class Device(
    @SerializedName("macAddress")
    @PrimaryKey(autoGenerate = false)
    val macAddress: String,
    @SerializedName("name")
    val name: String? = null
):Serializable