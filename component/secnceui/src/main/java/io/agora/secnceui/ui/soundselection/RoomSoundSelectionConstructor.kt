package io.agora.secnceui.ui.soundselection

import android.content.Context
import io.agora.secnceui.R
import io.agora.secnceui.annotation.SoundSelectionType
import io.agora.secnceui.bean.CustomerUsageBean
import io.agora.secnceui.bean.SoundSelectionBean

object RoomSoundSelectionConstructor {

    fun builderSoundSelectionList(
        context: Context,
        @SoundSelectionType currentSelection: Int
    ): MutableList<SoundSelectionBean> {

        val soundSelectionList = mutableListOf(
            SoundSelectionBean(
                soundSelection = SoundSelectionType.SocialChat,
                index = 0,
                soundName = context.getString(R.string.chatroom_social_chat),
                soundIntroduce = context.getString(R.string.chatroom_social_chat_introduce),
                isCurrentUsing = currentSelection == SoundSelectionType.SocialChat,
                customer = mutableListOf(
                    CustomerUsageBean("ya", R.drawable.icon_chatroom_ya_launcher),
                    CustomerUsageBean("soul", R.drawable.icon_chatroom_soul_launcher),
                )
            ),
            SoundSelectionBean(
                soundSelection = SoundSelectionType.Karaoke,
                index = 1,
                soundName = context.getString(R.string.chatroom_karaoke),
                soundIntroduce = context.getString(R.string.chatroom_karaoke_introduce),
                isCurrentUsing = currentSelection == SoundSelectionType.Karaoke,
                customer = mutableListOf(
                    CustomerUsageBean("ya", R.drawable.icon_chatroom_ya_launcher),
                    CustomerUsageBean("soul", R.drawable.icon_chatroom_soul_launcher),
                )
            ),
            SoundSelectionBean(
                soundSelection = SoundSelectionType.GamingBuddy,
                index = 2,
                soundName = context.getString(R.string.chatroom_gaming_buddy),
                soundIntroduce = context.getString(R.string.chatroom_gaming_buddy_introduce),
                isCurrentUsing = currentSelection == SoundSelectionType.GamingBuddy,
                customer = mutableListOf(
                    CustomerUsageBean("ya", R.drawable.icon_chatroom_ya_launcher),
                    CustomerUsageBean("soul", R.drawable.icon_chatroom_soul_launcher),
                )
            ),
            SoundSelectionBean(
                soundSelection = SoundSelectionType.ProfessionalBroadcaster,
                index = 2,
                soundName = context.getString(R.string.chatroom_professional_broadcaster),
                soundIntroduce = context.getString(R.string.chatroom_professional_broadcaster_introduce),
                isCurrentUsing = currentSelection == SoundSelectionType.ProfessionalBroadcaster,
                customer = mutableListOf(
                    CustomerUsageBean("ya", R.drawable.icon_chatroom_ya_launcher),
                    CustomerUsageBean("soul", R.drawable.icon_chatroom_soul_launcher),
                )
            )
        )
        val comparator: Comparator<SoundSelectionBean> = Comparator { o1, o2 ->
            o2.isCurrentUsing.compareTo(o1.isCurrentUsing)
        }
        soundSelectionList.sortWith(comparator)
        return soundSelectionList
    }

    fun builderCurSoundSelection(context: Context, @SoundSelectionType soundSelection: Int): SoundSelectionBean {
        return SoundSelectionBean(
            soundSelection = SoundSelectionType.SocialChat,
            index = 0,
            soundName = soundNameBySoundSelectionType(context, soundSelection),
            soundIntroduce = soundIntroduceBySoundSelectionType(context, soundSelection),
            isCurrentUsing = true,
            customer = mutableListOf(
                CustomerUsageBean("ya", R.drawable.icon_chatroom_ya_launcher),
                CustomerUsageBean("soul", R.drawable.icon_chatroom_soul_launcher)
            )
        )
    }

    private fun soundNameBySoundSelectionType(context: Context, @SoundSelectionType soundSelection: Int): String {
        return when (soundSelection) {
            SoundSelectionType.Karaoke -> {
                context.getString(R.string.chatroom_karaoke)
            }
            SoundSelectionType.GamingBuddy -> {
                context.getString(R.string.chatroom_gaming_buddy)
            }
            else -> {
                context.getString(R.string.chatroom_social_chat)
            }
        }
    }

    private fun soundIntroduceBySoundSelectionType(context: Context, @SoundSelectionType soundSelection: Int): String {
        return when (soundSelection) {
            SoundSelectionType.Karaoke -> {
                context.getString(R.string.chatroom_karaoke_introduce)
            }
            SoundSelectionType.GamingBuddy -> {
                context.getString(R.string.chatroom_gaming_buddy_introduce)
            }
            else -> {
                context.getString(R.string.chatroom_social_chat_introduce)
            }
        }
    }
}