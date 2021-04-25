package com.peer_messanger.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity
data class Device(
    @SerializedName("macAddress")
    @PrimaryKey(autoGenerate = false)
    var macAddress: String,
    @SerializedName("name")
    var name: String? = null,
    @Expose(serialize = false, deserialize = false)
    @Ignore
    var isSmartPhone: Boolean = true
) : Serializable {
    constructor() : this("",null)
}