package org.ledokol.testrunner.utils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

public class TimeConversion {
    public static void main(String[] args) {
        // Исходное время в формате ISO 8601
        String utcTime = "2024-03-26T22:58:37.451+00:00";

        // Парсинг исходного времени
        ZonedDateTime zonedUtcTime = ZonedDateTime.parse(utcTime);

        // Конвертация в московское время
        ZonedDateTime zonedMoscowTime = zonedUtcTime.withZoneSameInstant(ZoneId.of("Europe/Moscow"));

        // Форматирование для вывода
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedMoscowTime = zonedMoscowTime.format(formatter);

        System.out.println("Московское время: " + formattedMoscowTime);
    }
}

