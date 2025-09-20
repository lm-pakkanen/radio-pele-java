package com.lm_pakkanen.radio_pele_java.models

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.AbstractMap

internal class TrackInfoTest {
    @Test
    @DisplayName("Test TrackInfo.formatDuration")
    fun testFormatDuration() {
        val twoMinutesInMs = (2 * 60 * 1000).toLong()

        val durationsInMs: MutableList<AbstractMap.SimpleEntry<Long, String>> =
            object : ArrayList<AbstractMap.SimpleEntry<Long, String>>() {
                init {
                    add(createEntry(-1 * twoMinutesInMs, "<n/a>"))
                    add(createEntry(0, "<n/a>"))
                    add(createEntry(twoMinutesInMs, "2min"))
                    add(createEntry(twoMinutesInMs + 3 * 1000, "2min3s"))
                    add(createEntry(twoMinutesInMs + 30 * 1000, "2min30s"))
                }
            }

        for (durationInMs in durationsInMs) {
            val formatted = RefinedTrackInfo.formatDuration(durationInMs.key!!)
            Assertions.assertEquals(durationInMs.value, formatted)
        }
    }

    private fun createEntry(
        durationMs: Long,
        expectedDurationString: String,
  ): AbstractMap.SimpleEntry<Long, String> = AbstractMap.SimpleEntry<Long, String>(durationMs, expectedDurationString)
}
