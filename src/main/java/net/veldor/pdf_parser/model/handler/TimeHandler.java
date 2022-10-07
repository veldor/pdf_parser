package net.veldor.pdf_parser.model.handler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class TimeHandler {
    public static boolean isValidDate(String date) throws ParseException {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(format.parse(date)).equals(date);
    }

    public static boolean shotYearMoreThenCurrent(String s) {
        int shortYear = Integer.parseInt(s);
        int currentYear = Integer.parseInt(new SimpleDateFormat("yyyy").format(System.currentTimeMillis()).substring(2));
        return shortYear > currentYear;
    }
}
