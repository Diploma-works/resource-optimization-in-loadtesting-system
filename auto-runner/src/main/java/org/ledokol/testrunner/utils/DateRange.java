package org.ledokol.testrunner.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateRange {
    Date startDate;
    Date endDate;

    DateRange(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static DateRange[] getDefaultRanges() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateRange[] ranges;
        try {
            ranges = new DateRange[]{
                        new DateRange(sdf.parse("2024-03-28 13:00:00"), sdf.parse("2024-03-28 16:00:00")),
                        new DateRange(sdf.parse("2024-03-29 09:00:00"), sdf.parse("2024-03-29 14:00:00")),
                        new DateRange(sdf.parse("2024-03-30 15:00:00"), sdf.parse("2024-03-31 17:00:00")),
            };
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return ranges;
    }
}
