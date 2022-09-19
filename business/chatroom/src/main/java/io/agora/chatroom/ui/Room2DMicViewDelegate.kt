package io.agora.chatroom.ui

import android.app.Application
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import io.agora.baseui.adapter.OnItemClickListener
import io.agora.buddy.tool.ToastTools
import io.agora.buddy.tool.logE
import io.agora.chatroom.controller.RtcRoomController
import io.agora.chatroom.general.constructor.RoomInfoConstructor
import io.agora.chatroom.general.constructor.RoomInfoConstructor.isOwner
import io.agora.chatroom.general.repositories.ProfileManager
import io.agora.chatroom.model.ChatroomViewModel
import io.agora.chatroom.model.RoomMicViewModel
import io.agora.secnceui.R
import io.agora.secnceui.annotation.MicClickAction
import io.agora.secnceui.annotation.MicStatus
import io.agora.secnceui.bean.MicInfoBean
import io.agora.secnceui.bean.MicManagerBean
import io.agora.secnceui.ui.common.CommonFragmentAlertDialog
import io.agora.secnceui.ui.common.CommonSheetAlertDialog
import io.agora.secnceui.ui.mic.flat.IRoom2DMicView
import io.agora.secnceui.ui.micmanger.RoomMicManagerSheetDialog
import tools.DefaultValueCallBack
import tools.bean.VRoomBean
import tools.bean.VRoomInfoBean

/**
 * @author create by zhangwei03
 */
class Room2DMicViewDelegate constructor(
    private val activity: FragmentActivity,
    private val room2dMicView: IRoom2DMicView
) {

    companion object {
        private const val TAG = "Room2DMicViewDelegate"
    }

    private val roomAudioViewModel: RoomMicViewModel by lazy {
        ViewModelProvider(activity)[RoomMicViewModel::class.java]
    }

    var roomBean: VRoomBean.RoomsBean? = null
    private var vRoomBean: VRoomInfoBean? = null

    private val roomId by lazy {
        vRoomBean?.room?.room_id ?: ""
    }

    init {
        roomAudioViewModel.submitMicObservable().apply {

        }
    }

    private fun isOwner(): Boolean {
        val currentUid = ProfileManager.getInstance().profile?.uid
        val ownerUid = roomBean?.owner?.uid
        "currentUid：$currentUid ownerUid:$ownerUid".logE(TAG)
        return !currentUid.isNullOrEmpty() && TextUtils.equals(currentUid, ownerUid)
    }

    private fun isMicInfo(micInfo: MicInfoBean): Boolean {
        val currentUid = ProfileManager.getInstance().profile?.uid
        val micUid = micInfo.userInfo?.userId
        return !currentUid.isNullOrEmpty() && TextUtils.equals(currentUid, micUid)
    }

    fun onRoomDetails(vRoomBean: VRoomInfoBean) {
        this.vRoomBean = vRoomBean
        val micInfoList = RoomInfoConstructor.convertMicUiBean(vRoomBean)
        val botInfo = RoomInfoConstructor.convertMicBotUiBean(vRoomBean)
        room2dMicView.updateAdapter(micInfoList, mutableListOf(botInfo))
    }

    fun onUserMicClick(data: MicInfoBean, view: View, position: Int, viewType: Long) {
        if (data.micStatus == MicStatus.Idle && !isOwner()) {
            CommonSheetAlertDialog()
                .contentText(activity.getString(R.string.chatroom_request_speak))
                .rightText(activity.getString(R.string.chatroom_confirm))
                .leftText(activity.getString(R.string.chatroom_cancel))
                .setOnClickListener(object : CommonSheetAlertDialog.OnClickBottomListener {
                    override fun onConfirmClick() {
                        roomAudioViewModel.submitMic(activity, roomId, position)
                    }

                    override fun onCancelClick() {
                    }

                })
                .show(activity.supportFragmentManager, "CommonSheetAlertDialog")
        } else if (isOwner() || isMicInfo(data)) { // 房主或者自己
            RoomMicManagerSheetDialog(object : OnItemClickListener<MicManagerBean> {
                override fun onItemClick(data: MicManagerBean, view: View, position: Int, viewType: Long) {
                    when (data.micClickAction) {
                        MicClickAction.Invite -> {
//                            roomAudioViewModel.postInvitationMic()
                        }
                        MicClickAction.Mute -> {}
                        MicClickAction.UnMute -> {}
                        MicClickAction.Block -> {}
                        MicClickAction.UnBlock -> {}
                        MicClickAction.KickOff -> {}
                        MicClickAction.OffStage -> {}
                    }
                }
            }).apply {
                arguments = Bundle().apply {
                    putSerializable(RoomMicManagerSheetDialog.KEY_MIC_INFO, data)
                    putSerializable(RoomMicManagerSheetDialog.KEY_IS_OWNER, isOwner())
                }
                theme
            }.show(activity.supportFragmentManager, "RoomMicManagerSheetDialog")
        }
    }

    fun onBotMicClick(data: MicInfoBean, view: View, position: Int, viewType: Long) {
        if (RtcRoomController.get().botActivated()) {
            Toast.makeText(activity, "${data.userInfo?.username}", Toast.LENGTH_SHORT).show()
        } else {
            CommonFragmentAlertDialog()
                .titleText(activity.getString(R.string.chatroom_prompt))
                .contentText(activity.getString(R.string.chatroom_open_bot_prompt))
                .leftText(activity.getString(R.string.chatroom_cancel))
                .rightText(activity.getString(R.string.chatroom_confirm))
                .setOnClickListener(object : CommonFragmentAlertDialog.OnClickBottomListener {
                    override fun onConfirmClick() {
                        activeBot()
                    }
                })
                .show(activity.supportFragmentManager, "botActivatedDialog")
        }
    }

    private fun activeBot(active: Boolean = true) {
        RtcRoomController.get()
            .activeBot(roomAudioViewModel.getApplication(), active, object : DefaultValueCallBack<Boolean> {
                override fun onSuccess(isSuccess: Boolean) {
                    // TODO:
                    if (isSuccess) {
                        ToastTools.show(activity, "机器人激活成功")
                    } else {
                        ToastTools.show(activity, "机器人激活失败")
                    }
                }
            })
    }
}