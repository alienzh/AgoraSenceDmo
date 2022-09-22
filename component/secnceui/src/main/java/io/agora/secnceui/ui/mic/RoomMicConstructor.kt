package io.agora.secnceui.ui.mic

import android.content.Context
import io.agora.secnceui.R
import io.agora.secnceui.annotation.*
import io.agora.secnceui.bean.*
import io.agora.secnceui.constants.ScenesConstant

object RoomMicConstructor {

    fun builderDefault2dMicList(): MutableList<MicInfoBean> {

        return mutableListOf(
            MicInfoBean(index = 0),
            MicInfoBean(index = 1),
            MicInfoBean(index = 2),
            MicInfoBean(index = 3),
            MicInfoBean(index = 4),
            MicInfoBean(index = 5)
        )
    }

    fun builderDefault2dBotMicList(context: Context, isUserBot: Boolean = false): MutableList<BotMicInfoBean> {
        val blueBot = MicInfoBean(
            index = 6,
            micStatus = if (isUserBot) MicStatus.BotActivated else MicStatus.BotInactive,
            audioVolume = AudioVolumeStatus.None,
            userInfo = RoomUserInfoBean().apply {
                username = context.getString(R.string.chatroom_agora_blue)
                userAvatar = "icon_chatroom_blue_robot"
            }
        )
        val redBot = MicInfoBean(
            index = 7,
            micStatus = if (isUserBot) MicStatus.BotActivated else MicStatus.BotInactive,
            audioVolume = AudioVolumeStatus.None,
            userInfo = RoomUserInfoBean().apply {
                username = context.getString(R.string.chatroom_agora_red)
                userAvatar = "icon_chatroom_red_robot"
            }
        )
        return mutableListOf(BotMicInfoBean(blueBot, redBot))
    }

    fun builderDefault3dMicMap(context: Context, isUserBot: Boolean = false): MutableMap<String, MicInfoBean> {

        return mutableMapOf(
            ScenesConstant.KeyMic0 to MicInfoBean(index = 0),
            ScenesConstant.KeyMic1 to MicInfoBean(index = 1),
            ScenesConstant.KeyMicBlue to MicInfoBean(
                index = 2,
                micStatus = if (isUserBot) MicStatus.BotActivated else MicStatus.BotInactive,
                audioVolume = AudioVolumeStatus.None,
                userInfo = RoomUserInfoBean().apply {
                    username = context.getString(R.string.chatroom_agora_blue)
                    userAvatar = "icon_chatroom_blue_robot"
                }
            ),
            ScenesConstant.KeyMic3 to MicInfoBean(index = 3),
            ScenesConstant.KeyMic4 to MicInfoBean(index = 4),
            ScenesConstant.KeyMicRed to MicInfoBean(
                index = 5,
                micStatus = if (isUserBot) MicStatus.BotActivated else MicStatus.BotInactive,
                audioVolume = AudioVolumeStatus.None,
                userInfo = RoomUserInfoBean().apply {
                    username = context.getString(R.string.chatroom_agora_red)
                    userAvatar = "icon_chatroom_red_robot"
                }
            ),
            ScenesConstant.KeyMicCenter to MicInfoBean(index = 6),
        )
    }

    /**
     * 房主点击麦位管理
     */
    fun builderOwnerMicMangerList(
        context: Context, micInfo: MicInfoBean, isMyself: Boolean
    ): MutableList<MicManagerBean> {
        return when (micInfo.micStatus) {
            // 空置-开放
            MicStatus.Idle -> {
                mutableListOf(
                    MicManagerBean(context.getString(R.string.chatroom_invite), true, MicClickAction.Invite),
                    MicManagerBean(context.getString(R.string.chatroom_mute), true, MicClickAction.Mute),
                    MicManagerBean(context.getString(R.string.chatroom_block), true, MicClickAction.Block)
                )
            }
            // 座位禁⻨:有人、没人
            MicStatus.ForceMute -> {
                if (micInfo.userInfo == null) {
                    mutableListOf(
                        MicManagerBean(context.getString(R.string.chatroom_invite), true, MicClickAction.Invite),
                        MicManagerBean(context.getString(R.string.chatroom_unmute), true, MicClickAction.Mute),
                        MicManagerBean(context.getString(R.string.chatroom_block), true, MicClickAction.Block)
                    )
                } else {
                    mutableListOf(
                        MicManagerBean(context.getString(R.string.chatroom_kickoff), true, MicClickAction.KickOff),
                        MicManagerBean(context.getString(R.string.chatroom_unmute), true, MicClickAction.Mute),
                        MicManagerBean(context.getString(R.string.chatroom_block), true, MicClickAction.Block)
                    )
                }
            }
            // 空置-座位关闭
            MicStatus.Close -> {
                mutableListOf(
                    MicManagerBean(context.getString(R.string.chatroom_invite), false, MicClickAction.Invite),
                    MicManagerBean(context.getString(R.string.chatroom_mute), true, MicClickAction.Mute),
                    MicManagerBean(context.getString(R.string.chatroom_unblock), true, MicClickAction.UnBlock)
                )
            }
            // 空置-座位禁⻨&座位关闭
            MicStatus.CloseForceMute -> {
                mutableListOf(
                    MicManagerBean(context.getString(R.string.chatroom_invite), false, MicClickAction.Invite),
                    MicManagerBean(context.getString(R.string.chatroom_unmute), true, MicClickAction.Mute),
                    MicManagerBean(context.getString(R.string.chatroom_unblock), true, MicClickAction.UnBlock)
                )
            }
            // 正常
            MicStatus.Normal -> {
                if (isMyself) {
                    mutableListOf(
                        MicManagerBean(context.getString(R.string.chatroom_mute), true, MicClickAction.Mute)
                    )
                } else {
                    mutableListOf(
                        MicManagerBean(context.getString(R.string.chatroom_kickoff), true, MicClickAction.KickOff),
                        MicManagerBean(context.getString(R.string.chatroom_mute), true, MicClickAction.Mute),
                        MicManagerBean(context.getString(R.string.chatroom_block), true, MicClickAction.Block)
                    )
                }
            }
            // 禁麦
            MicStatus.Mute -> {
                if (isMyself) {
                    mutableListOf(
                        MicManagerBean(context.getString(R.string.chatroom_unmute), true, MicClickAction.UnMute)
                    )
                } else {
                    mutableListOf(
                        MicManagerBean(context.getString(R.string.chatroom_kickoff), true, MicClickAction.KickOff),
                        MicManagerBean(context.getString(R.string.chatroom_unmute), true, MicClickAction.Mute),
                        MicManagerBean(context.getString(R.string.chatroom_block), true, MicClickAction.Block)
                    )
                }
            }
            else -> mutableListOf()

        }
    }

    /**
     * 嘉宾点击麦位管理
     */
    fun builderGuestMicMangerList(context: Context, micInfo: MicInfoBean): MutableList<MicManagerBean> {
        return when (micInfo.micStatus) {
            // 有⼈-正常
            MicStatus.Normal -> {
                mutableListOf(
                    MicManagerBean(context.getString(R.string.chatroom_mute), true, MicClickAction.Mute),
                    MicManagerBean(context.getString(R.string.chatroom_off_stage), true, MicClickAction.OffStage)
                )
            }
            // 有⼈-禁麦
            MicStatus.ForceMute -> {
                // 被房主强制静音
                mutableListOf(
                    MicManagerBean(context.getString(R.string.chatroom_unmute), false, MicClickAction.Mute),
                    MicManagerBean(context.getString(R.string.chatroom_off_stage), true, MicClickAction.OffStage)
                )
            }
            else -> {
                mutableListOf(
                    MicManagerBean(context.getString(R.string.chatroom_unmute), true, MicClickAction.Mute),
                    MicManagerBean(context.getString(R.string.chatroom_off_stage), true, MicClickAction.OffStage)
                )
            }

        }
    }
}