package com.example.devopsai.risk;

import java.util.Locale;
/**
 * RiskLevel枚举，负责定义固定的业务取值。
 * 
 * @author zhang
 * @date 2026-06-29
 */

public enum RiskLevel {
    /**
     * 低风险，表示影响范围较小或可直接执行的操作。
     */
    LOW(1),
    /**
     * 中风险，表示需要确认影响范围后再执行的操作。
     */
    MEDIUM(2),
    /**
     * 高风险，表示可能影响服务稳定性或数据安全的操作。
     */
    HIGH(3),
    /**
     * 严重风险，表示可能造成重大服务中断或数据破坏的操作。
     */
    CRITICAL(4);

    /**
     * 风险等级优先级，数值越大风险越高。
     */
    private final int priority;

    RiskLevel(int priority) {
        this.priority = priority;
    }
    /**
     * 执行higherThan处理逻辑。
     * @param other other参数。
     * @return 处理结果。
     */

    public boolean higherThan(RiskLevel other) {
        return priority > other.priority;
    }
    /**
     * 执行parse处理逻辑。
     * @param value value参数。
     * @return 处理结果。
     */

    public static RiskLevel parse(String value) {
        if (value == null || value.isBlank()) {
            return LOW;
        }
        try {
            return RiskLevel.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return LOW;
        }
    }
    /**
     * 执行max处理逻辑。
     * @param left left参数。
     * @param right right参数。
     * @return 处理结果。
     */

    public static String max(String left, String right) {
        var leftLevel = parse(left);
        var rightLevel = parse(right);
        return rightLevel.higherThan(leftLevel) ? rightLevel.name() : leftLevel.name();
    }
}
