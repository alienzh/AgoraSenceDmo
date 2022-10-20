package io.agora.chatroom.controller

import android.content.Context
import io.agora.buddy.tool.ThreadManager
import io.agora.chatroom.BuildConfig
import io.agora.config.ConfigConstants
import io.agora.rtckit.open.IRtcKitListener
import io.agora.rtckit.open.RtcKitManager
import io.agora.rtckit.open.config.RtcChannelConfig
import io.agora.rtckit.open.config.RtcInitConfig
import io.agora.rtckit.open.event.RtcAudioEvent
import io.agora.rtckit.open.event.RtcDeNoiseEvent
import io.agora.rtckit.open.event.RtcSoundEffectEvent
import io.agora.rtckit.open.status.*
import io.agora.secnceui.bean.AINSModeBean
import io.agora.secnceui.bean.SoundAudioBean
import tools.ValueCallBack

/**
 * @author create by zhangwei03
 */
class RtcRoomController : IRtcKitListener {

    companion object {

        @JvmStatic
        fun get() = InstanceHelper.sSingle
    }

    object InstanceHelper {
        val sSingle = RtcRoomController()
    }

    private val rtcChannelConfig by lazy {
        RtcChannelConfig(BuildConfig.agora_app_token)
    }

    private var rtcManger: RtcKitManager? = null

    /**是否是主播*/
    var broadcaster = true

    /**本地audio 是否mute*/
    var isLocalAudioMute = false

    /**第一次启动机器，播放*/
    var firstActiveBot = true

    /**第一次切换ai 降噪*/
    var firstSwitchAnis = true

    /**降噪*/
    var anisMode = ConfigConstants.AINSMode.AINS_Medium

    /**是否开启机器人*/
    var isUseBot: Boolean = false

    /**机器人音量*/
    var botVolume: Int = ConfigConstants.RotDefaultVolume

    private var micVolumeListener: RtcMicVolumeListener? = null

    fun setMicVolumeListener(micVolumeListener: RtcMicVolumeListener) {
        this.micVolumeListener = micVolumeListener
    }

    private var joinCallback: ValueCallBack<Boolean>? = null

    /**加入rtc频道*/
    fun joinChannel(
        context: Context,
        roomId: String,
        userId: Int,
        broadcaster: Boolean = false,
        joinCallback: ValueCallBack<Boolean>
    ) {
        rtcManger = RtcKitManager.initRTC(context, RtcInitConfig(BuildConfig.agora_app_id), this)
        rtcChannelConfig.roomId = roomId
        rtcChannelConfig.userId = userId
        rtcChannelConfig.broadcaster = broadcaster
        this.joinCallback = joinCallback
        this.broadcaster = broadcaster
        rtcManger?.joinChannel(rtcChannelConfig)
    }

    fun switchRole(broadcaster: Boolean) {
        if (this.broadcaster == broadcaster) return
        rtcManger?.switchRole(broadcaster)
        this.broadcaster = broadcaster
    }

    /**
     * 降噪控制
     */
    fun deNoise(anisModeBean: AINSModeBean) {
        val event = when (anisModeBean.anisMode) {
            ConfigConstants.AINSMode.AINS_Off -> RtcDeNoiseEvent.CloseEvent()
            ConfigConstants.AINSMode.AINS_High -> RtcDeNoiseEvent.HighEvent()
            else -> RtcDeNoiseEvent.MediumEvent()
        }
        rtcManger?.operateDeNoise(event)
    }

    /**
     * 音效队列
     */
    private val soundAudioQueue: ArrayDeque<SoundAudioBean> = ArrayDeque()

    /**
     * 播放音效
     */
    fun playEffect(soundAudioList: List<SoundAudioBean>) {
        // 暂停其他音效播放
        rtcManger?.operateSoundEffect(RtcSoundEffectEvent.StopAllEffectEvent())
        // 加入音效队列
        soundAudioQueue.clear()
        soundAudioQueue.addAll(soundAudioList)
        // 取队列第一个播放
        soundAudioQueue.removeFirstOrNull()?.let {
            rtcManger?.operateSoundEffect(
                RtcSoundEffectEvent.PlayEffectEvent(
                    it.soundId, it.audioUrl, false, 1, it.speakerType
                )
            )
        }
    }

    /**
     * 播放音效
     */
    fun playEffect(soundId: Int, audioUrl: String, speakerType: Int) {
        // 暂停其他音效播放
        stopAllEffect()
        rtcManger?.operateSoundEffect(RtcSoundEffectEvent.PlayEffectEvent(soundId, audioUrl, false, 1, speakerType))
    }

    /**
     * 暂停所有音效播放
     */
    fun stopAllEffect() {
        soundAudioQueue.clear()
        rtcManger?.operateSoundEffect(RtcSoundEffectEvent.StopAllEffectEvent())
    }

    fun updateEffectVolume(volume:Int){
        rtcManger?.operateSoundEffect(RtcSoundEffectEvent.UpdateAudioEffectEvent(volume))
    }

    /**
     * 本地mute/unmute
     */
    fun enableLocalAudio(mute: Boolean) {
        isLocalAudioMute = mute
        rtcManger?.operateAudio(RtcAudioEvent.AudioMuteLocal(mute))
    }

    fun destroy() {
        // 退出房间恢复默认值
        firstActiveBot = true
        firstSwitchAnis = true
        anisMode = ConfigConstants.AINSMode.AINS_Medium
        isUseBot = false
        botVolume = ConfigConstants.RotDefaultVolume
        rtcManger?.leaveChannel()
        rtcManger?.destroy()
    }

    override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
        // 默认开启降噪
        val event = when (anisMode) {
            ConfigConstants.AINSMode.AINS_Off -> RtcDeNoiseEvent.CloseEvent()
            ConfigConstants.AINSMode.AINS_High -> RtcDeNoiseEvent.HighEvent()
            else -> RtcDeNoiseEvent.MediumEvent()
        }
        rtcManger?.operateDeNoise(event)
        joinCallback?.onSuccess(true)
    }

    override fun onLeaveChannel() {

    }

    override fun onConnectionStateChanged(state: Int, reason: Int) {

    }

    override fun onNetworkStatus(netWorkStatus: RtcNetWorkStatus) {

    }

    override fun onAudioStatus(audioChangeStatus: RtcAudioChangeStatus) {
    }

    override fun onUserJoin(userId: Int) {
    }


    override fun onLeaveChannel(userId: Int) {

    }

    override fun onAudioMixingFinished(soundId: Int, finished: Boolean, speakerType: Int) {
        if (finished) {
            // 结束播放回调--->>播放下一个，取队列第一个播放
            ThreadManager.getInstance().runOnMainThread {
                micVolumeListener?.onBotVolume(speakerType, true)
            }
            soundAudioQueue.removeFirstOrNull()?.let {
                rtcManger?.operateSoundEffect(
                    RtcSoundEffectEvent.PlayEffectEvent(it.soundId, it.audioUrl, false, 1, it.speakerType)
                )
            }
        } else {
            // 开始播放回调--->>
            ThreadManager.getInstance().runOnMainThread {
                micVolumeListener?.onBotVolume(speakerType, false)
            }
        }
    }

    override fun onError(rtcErrorStatus: RtcErrorStatus) {
    }

    override fun onAudioVolumeIndication(volumeIndicationStatus: RtcAudioVolumeIndicationStatus) {
        ThreadManager.getInstance().runOnMainThread {
            volumeIndicationStatus.speakers?.forEach { audioVolumeInfo ->
                if (audioVolumeInfo.volume == 0) {
                    micVolumeListener?.onUserVolume(audioVolumeInfo.uid, ConfigConstants.VolumeType.Volume_None)
                } else if (audioVolumeInfo.volume <= 60) {
                    micVolumeListener?.onUserVolume(audioVolumeInfo.uid, ConfigConstants.VolumeType.Volume_Low)
                } else if (audioVolumeInfo.volume <= 120) {
                    micVolumeListener?.onUserVolume(audioVolumeInfo.uid, ConfigConstants.VolumeType.Volume_Medium)
                } else if (audioVolumeInfo.volume <= 180) {
                    micVolumeListener?.onUserVolume(audioVolumeInfo.uid, ConfigConstants.VolumeType.Volume_High)
                } else {
                    micVolumeListener?.onUserVolume(audioVolumeInfo.uid, ConfigConstants.VolumeType.Volume_Max)
                }
            }
        }
    }
}