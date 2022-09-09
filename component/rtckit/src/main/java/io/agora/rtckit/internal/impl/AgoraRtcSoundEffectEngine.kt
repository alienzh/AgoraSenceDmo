package io.agora.rtckit.internal.impl

import io.agora.rtc2.RtcEngineEx
import io.agora.rtckit.internal.base.RtcBaseSoundEffectEngine

/**
 * @author create by zhangwei03
 *
 * agora 音效管理
 */
internal class AgoraRtcSoundEffectEngine : RtcBaseSoundEffectEngine<RtcEngineEx>() {
    override fun playEffect(soundId: Int, filePath: String, loopCount: Int, publish: Boolean): Boolean {
        return true
    }

    override fun stopEffect(soundId: Int): Boolean {
        return true
    }

    override fun pauseEffect(soundId: Int): Boolean {
        return true
    }

    override fun resumeEffect(soundId: Int): Boolean {
        return true
    }

    override fun stopAllEffect(): Boolean {
        return true
    }
}