package org.ledokol.testrunner.utils;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Component
public class TimeConversion {
    public long getUnixTimeFromMoscowZoneDate(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // Начальная и конечная дата в московском часовом поясе
        LocalDateTime date = LocalDateTime.parse(dateString, formatter); //2024-03-27 01:18:00

        // Преобразование дат в ZonedDateTime с учетом часового пояса
        ZonedDateTime startDateTime = date.atZone(ZoneId.of("Europe/Moscow"));

        // Преобразование в формат времени UNIX
        return startDateTime.toEpochSecond();
    }

}

