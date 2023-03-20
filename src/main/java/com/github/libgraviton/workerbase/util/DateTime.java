package com.github.libgraviton.workerbase.util;

import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDate;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


public class DateTime {
    final private DateFormat dateFormat;
    final private TimeZone timeZone;
    final private DateTimeFormatter formatter = new DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .optionalStart()
            .appendOffset("+HHMM", "+0000")
            .optionalEnd()
            .toFormatter();

    public DateTime(@NotNull TimeZone timeZone) {
        this.timeZone = timeZone;
        // this is the standard FIL date format (but can differ)
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(timeZone);
    }

    public @NotNull String getDate(@NotNull Date date) {
        return dateFormat.format(date);
    }

    public @NotNull Date getDate(@NotNull String date) {
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public @NotNull String getDate(@NotNull Date date, @NotNull String dateFormat) {
        DateFormat df = new SimpleDateFormat(dateFormat);
        df.setTimeZone(timeZone);

        return df.format(date);
    }

    public @NotNull Date parseISO8601(@NotNull String datetime) {
        return Date.from(OffsetDateTime.parse(datetime, formatter).toInstant());
    }

    /**
     * transforms a list of dates to its string versions (only date).
     *
     * @param dates list of dates
     *
     * @return list of dates in its string version (only date).
     */
    public @NotNull static ArrayList<String> getDatesAsStrings(@NotNull ArrayList<Date> dates) {
        ArrayList<String> _dates = new ArrayList<>();

        for (Date date: dates) {
            _dates.add((new LocalDate(date)).toString());
        }

        return _dates;
    }

    /**
     * transforms a list of dates to its string versions (only date).
     *
     * @param dates list of dates
     *
     * @return list of dates in its string version (only date).
     */
    public @NotNull static List<String> getLocalDatesAsStrings(@NotNull List<LocalDate> dates) {
        List<String> _dates = new ArrayList<>();

        for (LocalDate date: dates) {
            _dates.add(date.toString());
        }

        return _dates;
    }
}
