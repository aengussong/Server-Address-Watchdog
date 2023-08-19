package com.buttstuff.localserverwatchdog.ui.screen.model

data class UiInterval(val hours: Int, val minutes: Int) {
    val isEmpty: Boolean get() = hoursEmpty && minutes == 0
    val hoursEmpty: Boolean get() = hours == 0
}
