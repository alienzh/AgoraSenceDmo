package io.agora.rtckit.open.event

/**
 * @author create by zhangwei03
 *
 * AI 降噪操作事件
 */
sealed class RtcDeNoiseEvent {

    // 关闭降噪
    class CloseEvent: RtcDeNoiseEvent()
    // 中降噪
    class MediumEvent: RtcDeNoiseEvent()
    // 高降噪
    class HeightEvent: RtcDeNoiseEvent()

}
