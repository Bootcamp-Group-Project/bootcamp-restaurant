package com.restaurant.chat;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class MenuFetcher {

    private final JdbcTemplate jdbc;

    public MenuFetcher(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public String buildMenuPrompt() {
        StringBuilder sb = new StringBuilder();
        List<String> dishNames  = new ArrayList<>();
        List<String> drinkNames = new ArrayList<>();

        String today = toChineseDay(
            LocalDate.now(ZoneId.of("Asia/Hong_Kong")).getDayOfWeek().name());

        try {
            List<Map<String, Object>> dishes = jdbc.queryForList(
                "SELECT name, price, balance, status FROM dishes " +
                "WHERE (category = ? OR published = true) AND balance > 0 AND status = 'ON_LIST'",
                today);

            if (!dishes.isEmpty()) {
                sb.append("\n\n【今日主食菜單】");
                for (Map<String, Object> d : dishes) {
                    String line = formatItem(d);
                    sb.append("\n- ").append(line);
                    dishNames.add(String.valueOf(d.get("name")));
                }
            }
        } catch (Exception e) {
            System.out.println("[MenuFetcher] 主食查詢失敗：" + e.getMessage());
        }

        try {
            List<Map<String, Object>> drinks = jdbc.queryForList(
                "SELECT name, price, balance, status FROM drinks " +
                "WHERE balance > 0 AND status = 'ON_LIST'");

            if (!drinks.isEmpty()) {
                sb.append("\n\n【今日飲品菜單】");
                for (Map<String, Object> d : drinks) {
                    String line = formatItem(d);
                    sb.append("\n- ").append(line);
                    drinkNames.add(String.valueOf(d.get("name")));
                }
            }
        } catch (Exception e) {
            System.out.println("[MenuFetcher] 飲品查詢失敗：" + e.getMessage());
        }

        try {
            List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT slot1_start, slot1_end, slot2_start, slot2_end, enforced " +
                "FROM order_window_config WHERE id = 1");

            if (!rows.isEmpty()) {
                Map<String, Object> w = rows.get(0);
                boolean enforced = Boolean.TRUE.equals(w.get("enforced"));
                if (enforced) {
                    sb.append("\n\n【訂餐時間】");
                    sb.append("\n- ").append(w.get("slot1_start")).append(" – ").append(w.get("slot1_end"));
                    if (w.get("slot2_start") != null && w.get("slot2_end") != null) {
                        sb.append("\n- ").append(w.get("slot2_start")).append(" – ").append(w.get("slot2_end"));
                    }
                    sb.append("（香港時間）");
                } else {
                    sb.append("\n\n【訂餐時間】全日開放");
                }
            }
        } catch (Exception e) {
            System.out.println("[MenuFetcher] 訂餐時間查詢失敗：" + e.getMessage());
        }

        if (!dishNames.isEmpty() || !drinkNames.isEmpty()) {
            sb.append("\n\n⚠️ 今日餐牌完整清單如上，共主食 ").append(dishNames.size())
              .append(" 款、飲品 ").append(drinkNames.size()).append(" 款，不多不少。");
            sb.append("\n任何不在以上清單的菜式或飲品，今日一律沒有提供。");
            sb.append("\n介紹菜式時只可根據菜名創作描述，菜名、價格、庫存數字必須與清單完全一致。");
        } else {
            sb.append("\n\n⚠️ 今日餐牌尚未更新，請告知用戶稍後再查詢或聯絡餐廳職員。");
        }

        return sb.toString();
    }

    private String formatItem(Map<String, Object> d) {
        String name   = String.valueOf(d.get("name"));
        String price  = String.valueOf(d.get("price"));
        Object bal    = d.get("balance");
        String status = String.valueOf(d.get("status"));

        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" $").append(price);
        if ("SOLD_OUT".equals(status)) {
            sb.append("（售罄）");
        } else if (bal != null) {
            sb.append("（餘 ").append(bal).append(" 份）");
        }
        return sb.toString();
    }

    private static String toChineseDay(String englishDay) {
        return switch (englishDay) {
            case "MONDAY"    -> "週一";
            case "TUESDAY"   -> "週二";
            case "WEDNESDAY" -> "週三";
            case "THURSDAY"  -> "週四";
            case "FRIDAY"    -> "週五";
            case "SATURDAY"  -> "週六";
            default          -> englishDay;
        };
    }
}
