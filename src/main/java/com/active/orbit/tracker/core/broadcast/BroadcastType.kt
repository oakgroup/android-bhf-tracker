package com.active.orbit.tracker.core.broadcast

import androidx.annotation.StringDef

@Target(AnnotationTarget.EXPRESSION, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.LOCAL_VARIABLE)
@Retention(AnnotationRetention.SOURCE)
@StringDef(
    BaseBroadcast.DATA_UPDATED
)
annotation class BroadcastType