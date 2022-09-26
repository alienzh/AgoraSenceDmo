package io.agora.chatroom.ui

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.CompoundButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import io.agora.baseui.adapter.OnItemClickListener
import io.agora.baseui.general.callback.OnResourceParseCallback
import io.agora.baseui.general.net.Resource
import io.agora.baseui.interfaces.IParserSource
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
import io.agora.secnceui.annotation.AINSModeType
import io.agora.secnceui.annotation.MicClickAction
import io.agora.secnceui.annotation.MicStatus
import io.agora.secnceui.annotation.SoundSelectionType
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
        roomViewModel.openBotObservable.observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    "open bot onSuccess：$data".logE(TAG)
                    if (data != true) return
                    iRoomMicView.activeBot(true)
                    // 创建房间，第⼀次启动机器⼈后播放音效：
                    if (RtcRoomController.get().firstActiveBot) {
                        RtcRoomController.get().firstActiveBot = false
                        if (roomKitBean.roomType == ConfigConstants.Common_Chatroom) {
                            RoomSoundAudioConstructor.soundAudioMap[ConfigConstants.Audio_Create_Common_Room]?.let {
                                RtcRoomController.get().playEffect(it)
                            }
                        } else {
                            RoomSoundAudioConstructor.soundAudioMap[ConfigConstants.Audio_Create_Spatial_Room]?.let {
                                RtcRoomController.get().playEffect(it)
                            }
                        }
                    }
                }
            })
        }
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
                    iRoomMicView.updateBotVolume(speaker, ConfigConstants.Volume_None)
                } else {
                    iRoomMicView.updateBotVolume(speaker, ConfigConstants.Volume_Medium)
                }
            }
        })
    }

    /**
     * 详情
     */
    fun onRoomDetails(vRoomInfoBean: VRoomInfoBean?) {
        val isUseBot = vRoomInfoBean?.room?.use_robot ?: false
        RtcRoomController.get().isUseBot = isUseBot
        RtcRoomController.get().botVolume = vRoomInfoBean?.room?.robot_volume ?: 50
        RtcRoomController.get().soundEffect = vRoomInfoBean?.room?.getSoundSelection() ?: ConfigConstants.Social_Chat

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
        RoomContributionAndAudienceSheetDialog(activity).show(
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
            activity, SoundSelectionType.SocialChat
        )
        RoomSocialChatSheetDialog(object :
            RoomSocialChatSheetDialog.OnClickSocialChatListener {

            override fun onMoreSound() {
                RoomSpatialAudioSheetDialog(false).show(
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

            override fun onSoundEffect(@SoundSelectionType soundSelection: Int, isEnable: Boolean) {
                onSoundSelectionDialog(soundSelection, finishBack)
            }

            override fun onNoiseSuppression(@AINSModeType ainsModeType: Int, isEnable: Boolean) {
                onAINSDialog(ainsModeType)
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

    fun onSoundSelectionDialog(@SoundSelectionType soundSelection: Int, finishBack: () -> Unit) {
        RoomSoundSelectionSheetDialog(object :
            RoomSoundSelectionSheetDialog.OnClickSoundSelectionListener {

            override fun onSoundEffect(soundSelection: SoundSelectionBean) {
                onExitRoom(activity.getString(R.string.chatroom_exit_and_create_one), finishBack)
            }

        }, roomKitBean.isOwner).apply {
            arguments = Bundle().apply {
                putInt(RoomSoundSelectionSheetDialog.KEY_CURRENT_SELECTION, soundSelection)
            }
        }
            .show(activity.supportFragmentManager, "mtSoundSelection")
    }

    /**
     * AI降噪弹框
     */
    fun onAINSDialog(@AINSModeType ainsModeType: Int) {
        RoomAINSSheetDialog(roomKitBean.isOwner,
            anisModeCallback = {
                RtcRoomController.get().anisMode = it.anisMode
                RtcRoomController.get().deNoise(it)
            },
            anisSoundCallback = {
//                RtcRoomController.get().playEffect(it)
            }
        ).apply {
            arguments = Bundle().apply {
                putInt(RoomAINSSheetDialog.KEY_AINS_MODE, ainsModeType)
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
                            micViewModel.cancelLockMic(activity, roomKitBean.roomId)
                        }
                        MicClickAction.KickOff -> {
                            micViewModel.kickMic(
                                activity, roomKitBean.roomId, micInfo.userInfo?.userId ?: "", micInfo.index
                            )
                        }
                        MicClickAction.OffStage -> {
                            micViewModel.leaveMicMic(activity, roomKitBean.roomId)
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
    fun onBotMicClick(data: MicInfoBean, isUserBot: Boolean) {
        if (isUserBot) {
//            Toast.makeText(activity, "${data.userInfo?.username}", Toast.LENGTH_SHORT).show()
        } else {
            CommonFragmentAlertDialog()
                .titleText(activity.getString(R.string.chatroom_prompt))
                .contentText(activity.getString(R.string.chatroom_open_bot_prompt))
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