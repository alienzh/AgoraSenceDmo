package io.agora.chatroom.ui

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.CompoundButton
import android.widget.SeekBar
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import io.agora.baseui.adapter.OnItemClickListener
import io.agora.baseui.general.callback.OnResourceParseCallback
import io.agora.baseui.general.net.Resource
import io.agora.baseui.interfaces.IParserSource
import io.agora.buddy.tool.ToastTools
import io.agora.buddy.tool.logE
import io.agora.chatroom.bean.RoomKitBean
import io.agora.chatroom.controller.RtcMicVolumeListener
import io.agora.chatroom.controller.RtcRoomController
import io.agora.chatroom.general.constructor.RoomInfoConstructor
import io.agora.chatroom.general.repositories.ProfileManager
import io.agora.chatroom.model.ChatroomViewModel
import io.agora.chatroom.model.RoomMicViewModel
import io.agora.config.ConfigConstants
import io.agora.secnceui.R
import io.agora.secnceui.annotation.MicClickAction
import io.agora.secnceui.annotation.MicStatus
import io.agora.secnceui.bean.*
import io.agora.secnceui.ui.ainoise.RoomAINSSheetDialog
import io.agora.secnceui.ui.audiosettings.RoomAudioSettingsSheetDialog
import io.agora.secnceui.ui.common.CommonFragmentAlertDialog
import io.agora.secnceui.ui.common.CommonSheetAlertDialog
import io.agora.secnceui.ui.common.CommonSheetContentDialog
import io.agora.secnceui.ui.mic.IRoomMicView
import io.agora.secnceui.ui.micmanger.RoomMicManagerSheetDialog
import io.agora.secnceui.ui.rank.RoomContributionAndAudienceSheetDialog
import io.agora.secnceui.ui.soundselection.RoomSocialChatSheetDialog
import io.agora.secnceui.ui.soundselection.RoomSoundSelectionConstructor
import io.agora.secnceui.ui.soundselection.RoomSoundSelectionSheetDialog
import io.agora.secnceui.ui.spatialaudio.RoomSpatialAudioSheetDialog
import io.agora.secnceui.widget.top.IRoomLiveTopView
import tools.bean.VRoomInfoBean

/**
 * @author create by zhangwei03
 *
 * 房间头部 && 麦位置数据变化代理
 */
class RoomObservableViewDelegate constructor(
    private val activity: FragmentActivity,
    private val roomKitBean: RoomKitBean,
    private val iRoomTopView: IRoomLiveTopView, // 头部
    private val iRoomMicView: IRoomMicView, // 麦位
) : IParserSource {
    companion object {
        private const val TAG = "RoomObservableDelegate"
    }

    /**
     * room viewModel
     */
    private val roomViewModel: ChatroomViewModel by lazy {
        ViewModelProvider(activity)[ChatroomViewModel::class.java]
    }

    /**
     * mic viewModel
     */
    private val micViewModel: RoomMicViewModel by lazy {
        ViewModelProvider(activity)[RoomMicViewModel::class.java]
    }


    init {
        // 房间详情
        roomViewModel.roomDetailObservable.observe(activity) { response: Resource<VRoomInfoBean> ->
            parseResource(response, object : OnResourceParseCallback<VRoomInfoBean>() {

                override fun onSuccess(data: VRoomInfoBean?) {
                    "getRoomDetails onSuccess：$data".logE(TAG)
                    onRoomDetails(data)
                }
            })
        }
        // 打开机器人
        roomViewModel.openBotObservable.observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    "open bot onSuccess：$data".logE(TAG)
                    if (data != true) return
                    iRoomMicView.activeBot(true)
                    // 创建房间，第⼀次启动机器⼈后播放音效：
                    if (RtcRoomController.get().firstActiveBot) {
                        RtcRoomController.get().firstActiveBot = false
                        RoomSoundAudioConstructor.createRoomSoundAudioMap[roomKitBean.roomType]?.let {
                            RtcRoomController.get().playEffect(it)
                        }
                    }
                }
            })
        }
        // 关闭机器人
        roomViewModel.closeBotObservable.observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    "close bot onSuccess：$data".logE(TAG)
                    if (data != true) return
                    // 关闭机器人，暂停所有音效播放
                    iRoomMicView.activeBot(false)
                    RtcRoomController.get().stopAllEffect()
                }
            })
        }
        // 机器人音量
        roomViewModel.robotVolumeObservable.observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    "change robot volume：$data".logE()
                    if (data != true) return
                }
            })
        }
        // 麦位音量监听
        RtcRoomController.get().setMicVolumeListener(object : RtcMicVolumeListener() {
            // 更新机器人音量
            override fun onBotVolume(speaker: Int, finished: Boolean) {
                if (finished) {
                    iRoomMicView.updateBotVolume(speaker, ConfigConstants.VolumeType.Volume_None)
                } else {
                    iRoomMicView.updateBotVolume(speaker, ConfigConstants.VolumeType.Volume_Medium)
                }
            }
        })
        // 提交上麦申请监听
        micViewModel.submitMicObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    "submit mic：$data".logE()
                    if (data != true) return
                    ToastTools.show(activity, "submit mic:$data")
                }
            })
        }
        // 撤销上麦申请监听
        micViewModel.cancelSubmitMicObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    "cancel submit mic：$data".logE()
                    if (data != true) return
                    ToastTools.show(activity, "cancel submit mic:$data")
                }
            })
        }
        // 关麦
        micViewModel.closeMicObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    "close mic：$data".logE()
                    if (data != true) return
                    ToastTools.show(activity, "close mic:$data")
                }
            })
        }
        // 取消关麦
        micViewModel.cancelCloseMicObservable().observe(activity) { response: Resource<Pair<Int, Boolean>> ->
            parseResource(response, object : OnResourceParseCallback<Pair<Int, Boolean>>() {
                override fun onSuccess(data: Pair<Int, Boolean>?) {
                    "cancel close mic：$data".logE()
                    data?.let {
                        if (it.second) {
                            ToastTools.show(activity, "cancel close mic:${it.first}")
                        }
                    }

                }
            })
        }
        // 下麦
        micViewModel.leaveMicObservable().observe(activity) { response: Resource<Pair<Int, Boolean>> ->
            parseResource(response, object : OnResourceParseCallback<Pair<Int, Boolean>>() {
                override fun onSuccess(data: Pair<Int, Boolean>?) {
                    "leave mic：$data".logE()
                    data?.let {
                        if (it.second) {
                            ToastTools.show(activity, "leave mic:${it.first}")
                        }
                    }
                }
            })
        }
        // 禁言指定麦位
        micViewModel.muteMicObservable().observe(activity) { response: Resource<Pair<Int, Boolean>> ->
            parseResource(response, object : OnResourceParseCallback<Pair<Int, Boolean>>() {
                override fun onSuccess(data: Pair<Int, Boolean>?) {
                    "mute mic：$data".logE()
                    data?.let {
                        if (it.second) {
                            ToastTools.show(activity, "mute mic:${it.first}")
                        }
                    }
                }
            })
        }
        // 取消禁言指定麦位
        micViewModel.cancelMuteMicObservable().observe(activity) { response: Resource<Pair<Int, Boolean>> ->
            parseResource(response, object : OnResourceParseCallback<Pair<Int, Boolean>>() {
                override fun onSuccess(data: Pair<Int, Boolean>?) {
                    "cancel mute mic：$data".logE()
                    data?.let {
                        if (it.second) {
                            ToastTools.show(activity, "cancel mute mic:${it.first}")
                        }
                    }
                }
            })
        }
        // 交换麦位
        micViewModel.exChangeMicObservable().observe(activity) { response: Resource<Triple<Int, Int, Boolean>> ->
            parseResource(response, object : OnResourceParseCallback<Triple<Int, Int, Boolean>>() {
                override fun onSuccess(data: Triple<Int, Int, Boolean>?) {
                    "exchange mic：$data".logE()
                    data?.let {
                        if (it.third) {
                            ToastTools.show(activity, "exchange mic from:${it.first} to:${it.second}")
                        }
                    }
                }
            })
        }
        // 踢用户下麦
        micViewModel.kickMicObservable().observe(activity) { response: Resource<Pair<Int, Boolean>> ->
            parseResource(response, object : OnResourceParseCallback<Pair<Int, Boolean>>() {
                override fun onSuccess(data: Pair<Int, Boolean>?) {
                    "kick mic：$data".logE()
                    data?.let {
                        if (it.second) {
                            ToastTools.show(activity, "kick mic:${it.first}")
                        }
                    }
                }
            })
        }
        // 用户拒绝申请上麦
        micViewModel.rejectMicInvitationObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    "reject mic invitation：$data".logE()
                    if (data != true) return
                    ToastTools.show(activity, "reject mic invitation:$data")
                }
            })
        }
        // 锁麦
        micViewModel.lockMicObservable().observe(activity) { response: Resource<Pair<Int, Boolean>> ->
            parseResource(response, object : OnResourceParseCallback<Pair<Int, Boolean>>() {
                override fun onSuccess(data: Pair<Int, Boolean>?) {
                    "lock mic：$data".logE()
                    data?.let {
                        if (it.second) {
                            ToastTools.show(activity, "lock mic:${it.first}")
                        }
                    }
                }
            })
        }
        // 取消锁麦
        micViewModel.cancelLockMicObservable().observe(activity) { response: Resource<Pair<Int, Boolean>> ->
            parseResource(response, object : OnResourceParseCallback<Pair<Int, Boolean>>() {
                override fun onSuccess(data: Pair<Int, Boolean>?) {
                    "cancel lock mic：$data".logE()
                    data?.let {
                        if (it.second) {
                            ToastTools.show(activity, "cancel lock mic:${it.first}")
                        }
                    }
                }
            })
        }
        // 邀请上麦
        micViewModel.invitationMicObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    "invitation mic：$data".logE()
                    if (data != true) return
                    ToastTools.show(activity, "invitation mic:$data")
                }
            })
        }
        // 同意上麦申请
        micViewModel.applySubmitMicObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    "apply submit mic：$data".logE()
                    if (data != true) return
                    ToastTools.show(activity, "apply submit mic:$data")
                }
            })
        }
        // 拒绝上麦申请
        micViewModel.rejectSubmitMicObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    "reject submit mic：$data".logE()
                    if (data != true) return
                    ToastTools.show(activity, "reject submit mic:$data")
                }
            })
        }
    }

    /**
     * 详情
     */
    fun onRoomDetails(vRoomInfoBean: VRoomInfoBean?) {
        val isUseBot = vRoomInfoBean?.room?.use_robot ?: false
        RtcRoomController.get().isUseBot = isUseBot
        RtcRoomController.get().botVolume = vRoomInfoBean?.room?.robot_volume ?: 50
        RtcRoomController.get().soundEffect =
            vRoomInfoBean?.room?.getSoundSelection() ?: ConfigConstants.SoundSelection.Social_Chat

        val ownerUid = vRoomInfoBean?.room?.owner?.uid ?: ""
        vRoomInfoBean?.let {
            it.room?.let { vRoomInfo ->
                iRoomTopView.onChatroomInfo(RoomInfoConstructor.serverRoomInfo2UiRoomInfo(vRoomInfo))
            }
            it.mic_info?.let { micList ->
                iRoomMicView.updateAdapter(
                    RoomInfoConstructor.convertMicUiBean(micList, ownerUid), vRoomInfoBean.room?.use_robot ?: false
                )
            }
        }

    }

    /**
     * 排行榜
     */
    fun onClickRank(vRoomInfo: VRoomInfoBean?) {
        RoomContributionAndAudienceSheetDialog(activity, roomKitBean.isOwner).show(
            activity.supportFragmentManager,
            "ContributionAndAudienceSheetDialog"
        )
    }

    /**
     * 公告
     */
    fun onClickNotice(vRoomInfo: VRoomInfoBean?) {
        var notice = vRoomInfo?.room?.announcement ?: ""
        if (TextUtils.isEmpty(notice)) {
            notice = activity.getString(R.string.chatroom_first_enter_room_notice_tips)
        }
        CommonSheetContentDialog()
            .titleText(activity.getString(R.string.chatroom_notice))
            .contentText(notice)
            .show(activity.supportFragmentManager, "mtContentSheet")
    }

    /**
     * 音效
     */
    fun onClickSoundSocial(vRoomInfo: VRoomInfoBean?) {
        val curSoundSelection = RoomSoundSelectionConstructor.builderCurSoundSelection(
            activity, ConfigConstants.SoundSelection.Social_Chat
        )
        RoomSocialChatSheetDialog(object :
            RoomSocialChatSheetDialog.OnClickSocialChatListener {

            override fun onMoreSound() {
                RoomSpatialAudioSheetDialog(roomKitBean.isOwner).show(
                    activity.supportFragmentManager, "mtSpatialAudio"
                )
            }
        }).contentText(curSoundSelection.soundIntroduce)
            .customers(curSoundSelection.customer ?: mutableListOf())
            .show(activity.supportFragmentManager, "chatroomSocialChatSheetDialog")
    }

    /**
     * 音效设置
     */
    fun onAudioSettingsDialog(finishBack: () -> Unit) {
        RoomAudioSettingsSheetDialog(object :
            RoomAudioSettingsSheetDialog.OnClickAudioSettingsListener {

            override fun onBotCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                roomViewModel.activeBot(activity, roomKitBean.roomId, isChecked)
            }

            override fun onBotVolumeChange(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                roomViewModel.updateBotVolume(activity, roomKitBean.roomId, progress)
            }

            override fun onSoundEffect(soundSelectionType: Int, isEnable: Boolean) {
                onSoundSelectionDialog(soundSelectionType, finishBack)
            }

            override fun onNoiseSuppression(ainsMode: Int, isEnable: Boolean) {
                onAINSDialog(ainsMode)
            }

            override fun onSpatialAudio(isOpen: Boolean, isEnable: Boolean) {
                onSpatialDialog()
            }

        }).apply {
            arguments = Bundle().apply {
                val audioSettingsInfo = RoomAudioSettingsBean(
                    enable = roomKitBean.isOwner,
                    roomType = roomKitBean.roomType,
                    botOpen = RtcRoomController.get().isUseBot,
                    botVolume = RtcRoomController.get().botVolume,
                    soundSelection = RtcRoomController.get().soundEffect,
                    anisMode = RtcRoomController.get().anisMode,
                    spatialOpen = false
                )
                putSerializable(RoomAudioSettingsSheetDialog.KEY_AUDIO_SETTINGS_INFO, audioSettingsInfo)
            }
        }.show(activity.supportFragmentManager, "mtAudioSettings")
    }

    /**
     * 最佳音效选择
     */
    fun onSoundSelectionDialog(soundSelection: Int, finishBack: () -> Unit) {
        RoomSoundSelectionSheetDialog(roomKitBean.isOwner,
            object : RoomSoundSelectionSheetDialog.OnClickSoundSelectionListener {
                override fun onSoundEffect(soundSelection: SoundSelectionBean, isCurrentUsing: Boolean) {
                    if (isCurrentUsing) {
                        // 试听音效需要开启机器人
                        if (RtcRoomController.get().isUseBot) {
                            RoomSoundAudioConstructor.soundSelectionAudioMap[soundSelection.soundSelectionType]?.let {
                                RtcRoomController.get().playEffect(it)
                            }
                        } else {
                            onBotMicClick(false, activity.getString(R.string.chatroom_open_bot_to_sound_effect))
                        }
                    } else {
                        onExitRoom(activity.getString(R.string.chatroom_exit_and_create_one), finishBack)
                    }
                }

            }).apply {
            arguments = Bundle().apply {
                putInt(RoomSoundSelectionSheetDialog.KEY_CURRENT_SELECTION, soundSelection)
            }
        }
            .show(activity.supportFragmentManager, "mtSoundSelection")
    }

    /**
     * AI降噪弹框
     */
    fun onAINSDialog(ainsMode: Int) {
        RoomAINSSheetDialog(roomKitBean.isOwner,
            anisModeCallback = {
                RtcRoomController.get().anisMode = it.anisMode
                RtcRoomController.get().deNoise(it)
                "onAINSDialog anisModeCallback：$it".logE(TAG)
                if (roomKitBean.isOwner && RtcRoomController.get().isUseBot && RtcRoomController.get().firstSwitchAnis) {
                    RtcRoomController.get().firstSwitchAnis = false
                    RtcRoomController.get().playEffect(RoomSoundAudioConstructor.anisIntroduceAudioList)
                }
            },
            anisSoundCallback = { ainsSoundBean ->
                "onAINSDialog anisSoundCallback：$ainsSoundBean".logE(TAG)
                if (RtcRoomController.get().isUseBot) {
                    RoomSoundAudioConstructor.AINSSoundMap[ainsSoundBean.soundType]?.let { soundAudioBean ->
                        val audioUrl =
                            if (ainsSoundBean.soundMode == ConfigConstants.AINSMode.AINS_High) soundAudioBean.audioUrlHigh else soundAudioBean.audioUrl
                        // 试听降噪音效
                        RtcRoomController.get().playEffect(soundAudioBean.soundId, audioUrl, soundAudioBean.speakerType)
                    }
                } else {
                    onBotMicClick(false, activity.getString(R.string.chatroom_open_bot_to_sound_effect))
                }
            }
        ).apply {
            arguments = Bundle().apply {
                putInt(RoomAINSSheetDialog.KEY_AINS_MODE, ainsMode)
            }
        }
            .show(activity.supportFragmentManager, "mtAnis")
    }

    /**
     * 空间音频弹框
     */
    fun onSpatialDialog() {
        RoomSpatialAudioSheetDialog(roomKitBean.isOwner).apply {
            arguments = Bundle().apply {
                putBoolean(RoomSpatialAudioSheetDialog.KEY_SPATIAL_OPEN, false)
            }
        }
            .show(activity.supportFragmentManager, "mtSpatialAudio")
    }

    /**
     * 退出房间
     */
    fun onExitRoom(content: String, finishBack: () -> Unit) {
        CommonFragmentAlertDialog()
            .titleText(activity.getString(R.string.chatroom_prompt))
            .contentText(content)
            .leftText(activity.getString(R.string.chatroom_cancel))
            .rightText(activity.getString(R.string.chatroom_confirm))
            .setOnClickListener(object :
                CommonFragmentAlertDialog.OnClickBottomListener {
                override fun onConfirmClick() {
                    finishBack.invoke()
                }
            })
            .show(activity.supportFragmentManager, "mtCenterDialog")
    }

    /**
     * 点击麦位
     */
    fun onUserMicClick(micInfo: MicInfoBean) {
        if (micInfo.micStatus == MicStatus.Idle && !roomKitBean.isOwner) {
            CommonSheetAlertDialog()
                .contentText(activity.getString(R.string.chatroom_request_speak))
                .rightText(activity.getString(R.string.chatroom_confirm))
                .leftText(activity.getString(R.string.chatroom_cancel))
                .setOnClickListener(object : CommonSheetAlertDialog.OnClickBottomListener {
                    override fun onConfirmClick() {
                        micViewModel.submitMic(activity, roomKitBean.roomId, micInfo.index)
                    }

                    override fun onCancelClick() {
                    }

                })
                .show(activity.supportFragmentManager, "CommonSheetAlertDialog")
        } else if (roomKitBean.isOwner || ProfileManager.getInstance().isMyself(micInfo.userInfo?.userId)) { // 房主或者自己
            RoomMicManagerSheetDialog(object : OnItemClickListener<MicManagerBean> {
                override fun onItemClick(data: MicManagerBean, view: View, position: Int, viewType: Long) {
                    when (data.micClickAction) {
                        MicClickAction.Invite -> {
                            ToastTools.show(activity, "go to invite dialog")
                        }
                        MicClickAction.Mute -> {
                            if (ProfileManager.getInstance().isMyself(micInfo.userInfo?.userId)) { //自己静麦
                                RtcRoomController.get().enableLocalAudio(true)
                                micViewModel.closeMic(activity, roomKitBean.roomId, micInfo.index)
                            } else {
                                micViewModel.muteMic(activity, roomKitBean.roomId, micInfo.index)
                            }
                        }
                        MicClickAction.UnMute -> {
                            if (ProfileManager.getInstance().isMyself(micInfo.userInfo?.userId)) { //自己取消静麦
                                RtcRoomController.get().enableLocalAudio(false)
                                micViewModel.cancelCloseMic(activity, roomKitBean.roomId, micInfo.index)
                            } else {
                                micViewModel.cancelMuteMic(activity, roomKitBean.roomId, micInfo.index)
                            }
                        }
                        MicClickAction.Block -> {
                            micViewModel.lockMic(activity, roomKitBean.roomId, micInfo.index)
                        }
                        MicClickAction.UnBlock -> {
                            micViewModel.cancelLockMic(activity, roomKitBean.roomId, micInfo.index)
                        }
                        MicClickAction.KickOff -> {
                            micViewModel.kickMic(
                                activity, roomKitBean.roomId, micInfo.userInfo?.userId ?: "", micInfo.index
                            )
                        }
                        MicClickAction.OffStage -> {
                            micViewModel.leaveMicMic(activity, roomKitBean.roomId, micInfo.index)
                        }
                    }
                }
            }).apply {
                arguments = Bundle().apply {
                    putSerializable(RoomMicManagerSheetDialog.KEY_MIC_INFO, micInfo)
                    putSerializable(RoomMicManagerSheetDialog.KEY_IS_OWNER, roomKitBean.isOwner)
                    putSerializable(
                        RoomMicManagerSheetDialog.KEY_IS_MYSELF,
                        ProfileManager.getInstance().isMyself(micInfo.userInfo?.userId)
                    )
                }
            }.show(activity.supportFragmentManager, "RoomMicManagerSheetDialog")
        }
    }

    /**
     * 点击机器人
     */
    fun onBotMicClick(isUserBot: Boolean, content: String) {
        if (isUserBot) {
//            Toast.makeText(activity, "${data.userInfo?.username}", Toast.LENGTH_SHORT).show()
        } else {
            CommonFragmentAlertDialog()
                .titleText(activity.getString(R.string.chatroom_prompt))
                .contentText(content)
                .leftText(activity.getString(R.string.chatroom_cancel))
                .rightText(activity.getString(R.string.chatroom_confirm))
                .setOnClickListener(object : CommonFragmentAlertDialog.OnClickBottomListener {
                    override fun onConfirmClick() {
                        roomViewModel.activeBot(activity, roomKitBean.roomId, true)
                    }
                })
                .show(activity.supportFragmentManager, "botActivatedDialog")
        }
    }

}