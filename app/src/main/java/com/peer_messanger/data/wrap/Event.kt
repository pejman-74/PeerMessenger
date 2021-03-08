package com.peer_messanger.data.wrap

import java.util.concurrent.atomic.AtomicBoolean

/**
* for handle one time events.(mostly uses of live data observation)
* */
 class Event<out T>(private val content: T) {

    private val hasBeenHandled = AtomicBoolean(false)

    fun getContentIfNotHandled(handleContent: (T) -> Unit) {
        if (!hasBeenHandled.get()) {
            hasBeenHandled.set(true)
            handleContent(content)
        }
    }

    fun peekContent() = content
    fun isHasBeenHandled() = hasBeenHandled
}


