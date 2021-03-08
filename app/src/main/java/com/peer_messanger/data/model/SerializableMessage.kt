package com.peer_messanger.data.model

import com.google.gson.annotations.SerializedName

data class SerializableMessage(
    @SerializedName("messageId") val messageId: String,
    @SerializedName("messageBody") val messageBody: String
)
