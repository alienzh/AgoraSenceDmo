package io.agora.secnceui.widget.mic

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import io.agora.buddy.tool.ViewTools
import io.agora.secnceui.R
import io.agora.secnceui.annotation.MicStatus
import io.agora.secnceui.annotation.UserRole
import io.agora.secnceui.annotation.AudioVolumeStatus
import io.agora.secnceui.bean.MicInfoBean
import io.agora.secnceui.databinding.ViewChatroom3dMicBinding

/**
 * @author create by zhangwei03
 *
 * 3d麦位
 */
class Room3DMicView : ConstraintLayout {

    private lateinit var mBinding: ViewChatroom3dMicBinding

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context, attrs, defStyleAttr, defStyleRes
    ) {
        init(context)
    }

    private var arrowAnim: AnimationDrawable? = null

    private fun init(context: Context) {
        val root = View.inflate(context, R.layout.view_chatroom_3d_mic, this)
        mBinding = ViewChatroom3dMicBinding.bind(root)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        arrowAnim = mBinding.ivMicArrowAnim.background as AnimationDrawable?
        mBinding.ivMicArrowAnim.rotation = 360f
        arrowAnim?.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        arrowAnim?.stop()
        arrowAnim = null
    }

    fun binding(micInfo: MicInfoBean) {
        mBinding.apply {
            when (micInfo.micStatus) {
                MicStatus.ForceMute -> {
                    ivMicInnerIcon.isVisible = true
                    ivMicInnerIcon.setImageResource(R.drawable.icon_chatroom_mic_mute)
                    ivMicTag.isVisible = false
                    mtMicUsername.apply {
                        text = micInfo.index.toString()
                        setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    }
                }
                MicStatus.Lock -> {
                    ivMicInnerIcon.isVisible = true
                    ivMicInnerIcon.setImageResource(R.drawable.icon_chatroom_mic_close)
                    ivMicTag.isVisible = false
                    mtMicUsername.apply {
                        text = micInfo.index.toString()
                        setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    }
                }
                MicStatus.LockForceMute -> {
                    ivMicInnerIcon.isVisible = true
                    ivMicInnerIcon.setImageResource(R.drawable.icon_chatroom_mic_close)
                    ivMicTag.isVisible = true
                    ivMicTag.setImageResource(R.drawable.icon_chatroom_mic_mute_tag)
                    mtMicUsername.apply {
                        text = micInfo.index.toString()
                        setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    }
                }
                MicStatus.UserNormal -> {
                    ivMicInnerIcon.isVisible = false
                    mtMicUsername.text = micInfo.userInfo?.username ?: ""
                    ivMicInfo.setImageResource(
                        ViewTools.getDrawableId(ivMicInfo.context, micInfo.userInfo?.userAvatar ?: "")
                    )
                    ivMicTag.isVisible = false
                    if (micInfo.userInfo?.userRole == UserRole.Owner) {
                        mtMicUsername.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.icon_chatroom_mic_owner_tag, 0, 0, 0
                        )
                    } else {
                        mtMicUsername.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    }

                }
                MicStatus.UserForceMute -> {
                    ivMicInnerIcon.isVisible = false
                    mtMicUsername.text = micInfo.userInfo?.username ?: ""
                    ivMicInfo.setImageResource(
                        ViewTools.getDrawableId(ivMicInfo.context, micInfo.userInfo?.userAvatar ?: "")
                    )
                    ivMicTag.isVisible = true
                    ivMicTag.setImageResource(R.drawable.icon_chatroom_mic_mute_tag)
                    if (micInfo.userInfo?.userRole == UserRole.Owner) {
                        mtMicUsername.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.icon_chatroom_mic_owner_tag, 0, 0, 0
                        )
                    } else {
                        mtMicUsername.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    }
                }
                else -> {
                    ivMicInnerIcon.isVisible = true
                    ivMicInnerIcon.setImageResource(R.drawable.icon_chatroom_mic_add)
                    ivMicTag.isVisible = false
                    mtMicUsername.text = micInfo.index.toString()
                }
            }
            // 用户音量
            when (micInfo.audioVolume) {
//                AudioVolumeStatus.None -> ivMicTag.isVisible = false
                AudioVolumeStatus.Low -> {
                    ivMicTag.isVisible = true
                    ivMicTag.setImageResource(R.drawable.icon_chatroom_mic_open1)
                }
                AudioVolumeStatus.Middle -> {
                    ivMicTag.isVisible = true
                    ivMicTag.setImageResource(R.drawable.icon_chatroom_mic_open2)
                }
                AudioVolumeStatus.High -> {
                    ivMicTag.isVisible = true
                    ivMicTag.setImageResource(R.drawable.icon_chatroom_mic_open3)
                }
                AudioVolumeStatus.Max -> {
                    ivMicTag.isVisible = true
                    ivMicTag.setImageResource(R.drawable.icon_chatroom_mic_open4)
                }
                AudioVolumeStatus.Mute -> {
                    ivMicTag.isVisible = true
                    ivMicTag.setImageResource(R.drawable.icon_chatroom_mic_mute_tag)
                }
                else -> {

                }
            }


        }
    }

    fun changeAngle(angle: Float) {
        val layoutParams: LayoutParams = mBinding.ivMicArrowAnim.layoutParams as LayoutParams
        layoutParams.circleAngle = angle
        mBinding.ivMicArrowAnim.rotation = angle
        mBinding.ivMicArrowAnim.layoutParams = layoutParams
    }
}