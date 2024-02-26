package com.lm_pakkanen.radio_pele_java.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class TrackInfoTest {

  @Test
  @DisplayName("Test TrackInfo.formatDuration")
  public void testFormatDuration() {

    final long twoMinutesInMs = 2 * 60 * 1000;

    final List<AbstractMap.SimpleEntry<Long, String>> durationsInMs = new ArrayList<>() {
      {
        add(createEntry(-1 * twoMinutesInMs, "<n/a>"));
        add(createEntry(0, "<n/a>"));
        add(createEntry(twoMinutesInMs, "2min"));
        add(createEntry(twoMinutesInMs + 3 * 1000, "2min3s"));
        add(createEntry(twoMinutesInMs + 30 * 1000, "2min30s"));
      }
    };

    for (final AbstractMap.SimpleEntry<Long, String> durationInMs : durationsInMs) {
      String formatted = TrackInfo.formatDuration(durationInMs.getKey());
      assertEquals(durationInMs.getValue(), formatted);
    }
  }

  private @NonNull AbstractMap.SimpleEntry<Long, String> createEntry(
      long durationMs, @NonNull String expectedDurationString) {
    return new AbstractMap.SimpleEntry<>(durationMs, expectedDurationString);
  }

  @Configuration
  public static class Config {}
}
