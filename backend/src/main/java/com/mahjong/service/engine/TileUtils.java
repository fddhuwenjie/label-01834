package com.mahjong.service.engine;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 麻将牌工具类 — 牌型定义、洗牌、胡牌判定
 * 牌面编码: wan1~wan9, tiao1~tiao9, tong1~tong9, feng1~feng4(东南西北), jian1~jian3(中发白)
 * 每种牌各4张，共136张
 */
public class TileUtils {

    public static final String[] SUITS = {"wan", "tiao", "tong"};
    public static final String[] WIND_NAMES = {"东", "南", "西", "北"};
    public static final String[] DRAGON_NAMES = {"中", "发", "白"};

    public static List<String> createFullDeck() {
        List<String> deck = new ArrayList<>(136);
        for (String suit : SUITS) {
            for (int v = 1; v <= 9; v++) {
                for (int i = 0; i < 4; i++) {
                    deck.add(suit + v);
                }
            }
        }
        for (int v = 1; v <= 4; v++) {
            for (int i = 0; i < 4; i++) {
                deck.add("feng" + v);
            }
        }
        for (int v = 1; v <= 3; v++) {
            for (int i = 0; i < 4; i++) {
                deck.add("jian" + v);
            }
        }
        return deck;
    }

    public static void shuffle(List<String> deck) {
        Collections.shuffle(deck, new Random());
    }

    /** 获取牌的花色 */
    public static String getSuit(String tile) {
        if (tile.startsWith("wan")) return "wan";
        if (tile.startsWith("tiao")) return "tiao";
        if (tile.startsWith("tong")) return "tong";
        if (tile.startsWith("feng")) return "feng";
        if (tile.startsWith("jian")) return "jian";
        return "";
    }

    /** 获取牌的数值 */
    public static int getValue(String tile) {
        String suit = getSuit(tile);
        return Integer.parseInt(tile.substring(suit.length()));
    }

    /** 是否为数牌（万/条/筒） */
    public static boolean isNumberTile(String tile) {
        String suit = getSuit(tile);
        return "wan".equals(suit) || "tiao".equals(suit) || "tong".equals(suit);
    }

    /** 获取牌的显示名称 */
    public static String getDisplayName(String tile) {
        String suit = getSuit(tile);
        int value = getValue(tile);
        String[] numNames = {"", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
        return switch (suit) {
            case "wan" -> numNames[value] + "万";
            case "tiao" -> numNames[value] + "条";
            case "tong" -> numNames[value] + "筒";
            case "feng" -> WIND_NAMES[value - 1];
            case "jian" -> DRAGON_NAMES[value - 1];
            default -> tile;
        };
    }

    /**
     * 判断手牌是否胡牌（14张牌）
     * 支持两种胡牌形式：
     * 1) 标准胡牌：4 面子 + 1 雀头（会遍历所有可能的雀头选择，避免遗漏）
     * 2) 七对子：7 组互不相同的对子
     *
     * 已知限制：本方法仅基于 14 张手牌做静态判定，不区分"自摸"与"点炮"场景。
     * 调用方（如 MahjongEngine）当前在自摸（手牌 14 张）和点炮（手牌 13 张 + 别家打出 1 张）
     * 时都通过组合成 14 张后调用本方法或 canWinWith()，因此点炮与自摸的番数差异
     * （如自摸番、抢杠番等）目前未在引擎层区分，需要在后续迭代中由上层结算逻辑补齐。
     */
    public static boolean isWinningHand(List<String> hand) {
        if (hand.size() != 14) return false;
        Map<String, Integer> counts = toCountMap(hand);

        // 优先判断七对子特殊胡牌形式
        if (isSevenPairs(counts)) return true;

        // 标准胡牌：完整遍历所有可能的雀头选择
        for (String pairTile : new ArrayList<>(counts.keySet())) {
            if (counts.getOrDefault(pairTile, 0) >= 2) {
                counts.merge(pairTile, -2, Integer::sum);
                boolean ok = canDecomposeMelds(counts, 0);
                counts.merge(pairTile, 2, Integer::sum);
                if (ok) return true;
            }
        }
        return false;
    }

    /**
     * 检查手牌加上指定牌后是否可以胡
     * 已知限制：未区分自摸 / 点炮场景的番数差异，仅做能否胡牌的结构性判定。
     */
    public static boolean canWinWith(List<String> hand, String tile) {
        List<String> combined = new ArrayList<>(hand);
        combined.add(tile);
        return isWinningHand(combined);
    }

    private static Map<String, Integer> toCountMap(List<String> tiles) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (String t : tiles) {
            map.merge(t, 1, Integer::sum);
        }
        return map;
    }

    /**
     * 判定七对子：手牌恰好由 7 组互不相同的对子组成
     */
    private static boolean isSevenPairs(Map<String, Integer> counts) {
        int pairCount = 0;
        int total = 0;
        for (int c : counts.values()) {
            if (c == 0) continue;
            if (c != 2) return false;
            pairCount++;
            total += c;
        }
        return pairCount == 7 && total == 14;
    }

    /**
     * 递归拆解剩余手牌为若干面子（刻子或顺子）
     * 调用前已扣除雀头，因此本方法只关心 4 个面子的拆解。
     * @param counts 牌计数
     * @param extracted 已提取的面子数
     */
    private static boolean canDecomposeMelds(Map<String, Integer> counts, int extracted) {
        int remaining = counts.values().stream().mapToInt(Integer::intValue).sum();
        if (remaining == 0 && extracted == 4) return true;
        if (remaining == 0) return false;

        String first = counts.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
        if (first == null) return false;

        if (counts.get(first) >= 3) {
            counts.merge(first, -3, Integer::sum);
            if (canDecomposeMelds(counts, extracted + 1)) return true;
            counts.merge(first, 3, Integer::sum);
        }

        if (isNumberTile(first)) {
            String suit = getSuit(first);
            int val = getValue(first);
            if (val <= 7) {
                String t2 = suit + (val + 1);
                String t3 = suit + (val + 2);
                if (counts.getOrDefault(t2, 0) > 0 && counts.getOrDefault(t3, 0) > 0) {
                    counts.merge(first, -1, Integer::sum);
                    counts.merge(t2, -1, Integer::sum);
                    counts.merge(t3, -1, Integer::sum);
                    if (canDecomposeMelds(counts, extracted + 1)) return true;
                    counts.merge(first, 1, Integer::sum);
                    counts.merge(t2, 1, Integer::sum);
                    counts.merge(t3, 1, Integer::sum);
                }
            }
        }

        return false;
    }

    /** 检查是否可以碰（手中有2张相同牌） */
    public static boolean canPong(List<String> hand, String tile) {
        return hand.stream().filter(t -> t.equals(tile)).count() >= 2;
    }

    /** 检查是否可以杠（手中有3张相同牌） */
    public static boolean canKong(List<String> hand, String tile) {
        return hand.stream().filter(t -> t.equals(tile)).count() >= 3;
    }

    /** 检查是否可以暗杠（手中有4张相同牌） */
    public static List<String> findSelfKong(List<String> hand) {
        Map<String, Long> counts = hand.stream().collect(Collectors.groupingBy(t -> t, Collectors.counting()));
        return counts.entrySet().stream()
                .filter(e -> e.getValue() == 4)
                .map(Map.Entry::getKey)
                .toList();
    }

    /** 检查是否可以吃（仅上家打出的牌，且为数牌） */
    public static List<List<String>> findChi(List<String> hand, String tile) {
        List<List<String>> results = new ArrayList<>();
        if (!isNumberTile(tile)) return results;

        String suit = getSuit(tile);
        int val = getValue(tile);
        Map<String, Long> counts = hand.stream().collect(Collectors.groupingBy(t -> t, Collectors.counting()));

        if (val >= 3 && counts.getOrDefault(suit + (val - 2), 0L) > 0
                && counts.getOrDefault(suit + (val - 1), 0L) > 0) {
            results.add(List.of(suit + (val - 2), suit + (val - 1), tile));
        }
        if (val >= 2 && val <= 8 && counts.getOrDefault(suit + (val - 1), 0L) > 0
                && counts.getOrDefault(suit + (val + 1), 0L) > 0) {
            results.add(List.of(suit + (val - 1), tile, suit + (val + 1)));
        }
        if (val <= 7 && counts.getOrDefault(suit + (val + 1), 0L) > 0
                && counts.getOrDefault(suit + (val + 2), 0L) > 0) {
            results.add(List.of(tile, suit + (val + 1), suit + (val + 2)));
        }
        return results;
    }

    /** 对手牌排序（按花色和点数） */
    public static void sortHand(List<String> hand) {
        hand.sort((a, b) -> {
            String sa = getSuit(a), sb = getSuit(b);
            int order = suitOrder(sa) - suitOrder(sb);
            if (order != 0) return order;
            return getValue(a) - getValue(b);
        });
    }

    private static int suitOrder(String suit) {
        return switch (suit) {
            case "wan" -> 0;
            case "tiao" -> 1;
            case "tong" -> 2;
            case "feng" -> 3;
            case "jian" -> 4;
            default -> 5;
        };
    }
}
