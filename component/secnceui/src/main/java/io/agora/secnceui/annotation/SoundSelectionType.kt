package io.agora.secnceui.annotation

import androidx.annotation.IntDef

@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
@IntDef(
    SoundSelectionType.SocialChat,
    SoundSelectionType.Karaoke,
    SoundSelectionType.GamingBuddy
)
annotation class SoundSelectionType {
    companion object {
        const val SocialChat = 0

        const val Karaoke = 1

        const val GamingBuddy = 2
    }
}

