package com.yll.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 时间解析器
 * 将中文时间字符串解析为小时数
 */
public class TimeParser {

    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)天|\\s*(\\d+)时|\\s*(\\d+)分|\\s*(\\d+)秒");

    /**
     * 将时间字符串解析为小时数
     * 支持格式：X天 Y时 Z分 A秒
     *
     * @param timeString 时间字符串
     * @return 总小时数
     */
    public static double parseToHours(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            return 0.0;
        }

        Matcher matcher = TIME_PATTERN.matcher(timeString);

        double days = 0;
        double hours = 0;
        double minutes = 0;
        double seconds = 0;

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                days = Double.parseDouble(matcher.group(1));
            } else if (matcher.group(2) != null) {
                hours = Double.parseDouble(matcher.group(2));
            } else if (matcher.group(3) != null) {
                minutes = Double.parseDouble(matcher.group(3));
            } else if (matcher.group(4) != null) {
                seconds = Double.parseDouble(matcher.group(4));
            }
        }

        return days * 24 + hours + minutes / 60.0 + seconds / 3600.0;
    }

    /**
     * 格式化时间为可读字符串
     *
     * @param hours 小时数
     * @return 格式化后的时间字符串
     */
    public static String formatTime(double hours) {
        if (hours > 24) {
            double days = hours / 24;
            return String.format("%.1fd", days);
        } else {
            return String.format("%.1fh", hours);
        }
    }
}
