package io.agora.secnceui.annotation

import androidx.annotation.IntDef

/**
 * 降噪开启/关闭
 */
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
@IntDef(AINSSoundType.AINS, AINSSoundType.None, AINSSoundType.Audition)
annotation class AINSSoundType {
    companion object {
        const val AINS = 0

        const val None = 1

        const val Audition = 2
    }
}
