package com.buttstuff.localserverwatchdog.ui.screen.set_interval

import java.util.concurrent.TimeUnit

class IntervalBuilder {
    private var hours = 0L
    private var minutes = 0L

    fun set(milliseconds: Long) {
        hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60
    }

    fun buildUI(): UiInterval = UiInterval(hours.toInt(), minutes.toInt())
    fun toMillis() = TimeUnit.HOURS.toMillis(hours) + TimeUnit.MINUTES.toMillis(minutes)
    fun handleCommand(command: Command) {
        when (command) {
            is Number -> {
                // user manually entered max line: 10:00, where we can't append anymore
                if (hours > 9) return

                val digit = command.number
                // shift minutes to the left, leftmost minutes digit will be moved to hours
                val movedMinutes = minutes * 10 + digit
                // digit that was shifted to hours
                val shiftedToHours = movedMinutes / 100
                // drop shifted to hours part
                minutes = movedMinutes % 100

                val movedHours = hours * 10 + shiftedToHours
                hours = movedHours % 100
            }

            is Erase -> {
                val shiftedHours = hours % 10
                hours /= 10

                minutes /= 10
                minutes += shiftedHours * 10
            }

            is Done -> Unit //noop
        }
    }

    fun isValid() = hours != 0L || minutes >= 5L
}
