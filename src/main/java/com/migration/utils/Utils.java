package com.migration.utils;

public class Utils {
    public static String getTimeElapsed(long elapsed) {
        long milliseconds = elapsed % 1000;
        elapsed = elapsed / 1000;
        long seconds = elapsed % 60;
        elapsed = elapsed / 60;
        long minutes = elapsed % 60;
        elapsed = elapsed / 60;
        long hours = elapsed % 24;
        elapsed = elapsed / 24;
        long days = elapsed % 7;
        elapsed = elapsed / 7;
        long weeks = elapsed % 4;
        elapsed = elapsed / 4;
        long months = elapsed % 12;
        long years = elapsed / 12;

        String millisStr = (milliseconds != 0? (milliseconds + " ms") : "");
        String secondsStr = (seconds != 0 ? (seconds + " s ") : "");
        String minutesStr = (minutes != 0 ? (minutes + " m ") : "");
        String hoursStr = (hours != 0 ? (hours + " h ") : "");
        String daysStr = (days != 0 ? (days + " d ") : "");
        String weeksStr = (weeks != 0 ? (weeks + " w ") : "");
        String monthsStr = (months != 0 ? (months + " M ") : "");
        String yearsStr = (years != 0 ? (years + " y ") : "");

        String result = new StringBuilder(yearsStr)
                .append(monthsStr)
                .append(weeksStr)
                .append(daysStr)
                .append(hoursStr)
                .append(minutesStr)
                .append(secondsStr)
                .append(millisStr).toString();

        return result.isEmpty()?"0 ms":result;
    }
}
