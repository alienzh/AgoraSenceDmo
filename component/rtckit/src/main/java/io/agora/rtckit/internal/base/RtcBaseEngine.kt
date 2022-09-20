package io.agora.rtckit.internal.base

import io.agora.rtckit.constants.RtcKitConstant
import io.agora.rtckit.internal.IRtcClientListener

/**
 * @author create by zhangwei03
 *
 * base engine eg,audioEngine,audio
 */
internal abstract class RtcBaseEngine<T> {

    companion object{
        const val TAG = "${RtcKitConstant.TAG_PREFIX} RtcEngine"
    }

    protected var engine: T? = null
    protected var listener: IRtcClientListener? = null

    fun attach(client: T?, listener: IRtcClientListener?) {
        this.engine = client
        this.listener = listener
    }

    fun detach() {
        this.engine = null
        this.listener = null
    }
}