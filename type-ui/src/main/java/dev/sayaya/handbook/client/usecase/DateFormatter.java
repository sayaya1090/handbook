package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.LayoutPeriod;
import dev.sayaya.handbook.domain.Type;
import elemental2.core.JsDate;

/**
 * 날짜 및 기간 정보를 사용자 친화적인 형식(YYYY-MM-DD)으로 변환하는 유틸리티.
 */
public class DateFormatter {
    public static String formatRange(LayoutPeriod period) {
        if (period == null) return "";
        return formatRange(period.effectDateTime(), period.expireDateTime());
    }

    public static String formatRange(Type type) {
        if (type == null) return "";
        return formatRange(type.effectDateTime(), type.expireDateTime());
    }

    public static String formatRange(double start, double end) {
        return format(start) + " ~ " + format(end);
    }

    public static String format(double timestamp) {
        if (timestamp >= 253402214400000.0) return "∞"; // 9999-12-31 근처
        if (timestamp <= 0) return "-∞";
        JsDate date = new JsDate(timestamp);
        return date.getFullYear() + "-" + pad(date.getMonth() + 1) + "-" + pad(date.getDate());
    }

    public static double parse(String value) {
        if (value == null || value.trim().isEmpty() || "∞".equals(value.trim())) {
            return 253402214400000.0;
        }
        if ("-∞".equals(value.trim())) return 0.0;
        
        try {
            // YYYY-MM-DD 직접 파싱 (GWT JsDate 호환성 고려)
            String[] parts = value.trim().split("-");
            if (parts.length == 3) {
                int y = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]) - 1;
                int d = Integer.parseInt(parts[2]);
                JsDate manual = new JsDate(y, m, d);
                manual.setHours(0, 0, 0, 0);
                double time = manual.getTime();
                if (Double.isNaN(time)) throw new IllegalArgumentException("Invalid date value");
                elemental2.dom.DomGlobal.console.log("[DateFormatter] Parsed " + value + " to " + time);
                return time;
            }
            // 폴백: 브라우저 기본 파싱
            JsDate date = new JsDate(value.trim());
            if (!Double.isNaN(date.getTime())) return date.getTime();
            throw new IllegalArgumentException("Invalid date format");
        } catch (Exception e) {
            elemental2.dom.DomGlobal.console.error("[DateFormatter] Error parsing date: " + value, e);
            throw new IllegalArgumentException("Invalid date format: " + value);
        }
    }

    private static String pad(int n) { return n < 10 ? "0" + n : "" + n; }
}
