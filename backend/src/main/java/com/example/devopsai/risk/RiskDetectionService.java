package com.example.devopsai.risk;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.devopsai.diagnosis.DiagnosisController.CommandSuggestion;
import com.example.devopsai.risk.entity.RiskRule;
import com.example.devopsai.risk.mapper.RiskRuleMapper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
/**
 * RiskDetectionService服务类，负责封装对应模块的业务逻辑。
 * 
 * @author zhang
 * @date 2026-06-29
 */

@Service
public class RiskDetectionService {

    /**
     * 风险规则数据访问对象。
     */
    private final RiskRuleMapper riskRuleMapper;
    /**
     * 创建RiskDetectionService实例。
     * @param riskRuleMapper riskRuleMapper参数。
     */

    public RiskDetectionService(RiskRuleMapper riskRuleMapper) {
        this.riskRuleMapper = riskRuleMapper;
    }
    /**
     * 检测文本风险等级。
     * @param commands commands参数。
     * @param rawTexts rawTexts参数。
     * @return 处理结果。
     */

    public DetectionResult detect(List<CommandSuggestion> commands, List<String> rawTexts) {
        var rules = enabledRules();
        var warnings = new LinkedHashSet<String>();
        var highestRisk = "LOW";
        var enrichedCommands = new ArrayList<CommandSuggestion>();

        for (var command : safeList(commands)) {
            var commandRisk = command.riskLevel();
            var commandWarning = command.warning();
            for (var rule : rules) {
                if (matches(rule, command.command())) {
                    commandRisk = RiskLevel.max(commandRisk, rule.getRiskLevel());
                    if (StringUtils.hasText(rule.getWarningMessage())) {
                        commandWarning = mergeWarning(commandWarning, rule.getWarningMessage());
                        warnings.add(rule.getWarningMessage());
                    }
                }
            }
            highestRisk = RiskLevel.max(highestRisk, commandRisk);
            if (StringUtils.hasText(commandWarning)) {
                warnings.add(commandWarning);
            }
            enrichedCommands.add(new CommandSuggestion(command.command(), command.description(), commandRisk, commandWarning));
        }

        for (var text : safeList(rawTexts)) {
            if (!StringUtils.hasText(text)) {
                continue;
            }
            for (var rule : rules) {
                if (matches(rule, text)) {
                    highestRisk = RiskLevel.max(highestRisk, rule.getRiskLevel());
                    if (StringUtils.hasText(rule.getWarningMessage())) {
                        warnings.add(rule.getWarningMessage());
                    }
                }
            }
        }

        return new DetectionResult(enrichedCommands, highestRisk, List.copyOf(warnings));
    }
    /**
     * 执行safeList处理逻辑。
     * @param values values参数。
     * @return 处理结果。
     */

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }
    /**
     * 执行enabledRules处理逻辑。
     * @return 处理结果。
     */

    private List<RiskRule> enabledRules() {
        return riskRuleMapper.selectList(new LambdaQueryWrapper<RiskRule>()
                .eq(RiskRule::getStatus, 1)
                .eq(RiskRule::getDeleted, 0)
                .orderByAsc(RiskRule::getId));
    }
    /**
     * 执行matches处理逻辑。
     * @param rule rule参数。
     * @param text text参数。
     * @return 处理结果。
     */

    private boolean matches(RiskRule rule, String text) {
        if (!StringUtils.hasText(rule.getPattern()) || !StringUtils.hasText(text)) {
            return false;
        }
        if ("REGEX".equalsIgnoreCase(rule.getPatternType())) {
            try {
                return Pattern.compile(rule.getPattern(), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
                        .matcher(text)
                        .find();
            } catch (PatternSyntaxException ignored) {
                return false;
            }
        }
        return text.toLowerCase(Locale.ROOT).contains(rule.getPattern().toLowerCase(Locale.ROOT));
    }
    /**
     * 执行mergeWarning处理逻辑。
     * @param original original参数。
     * @param detected detected参数。
     * @return 处理结果。
     */

    private String mergeWarning(String original, String detected) {
        if (!StringUtils.hasText(original)) {
            return detected;
        }
        if (original.contains(detected)) {
            return original;
        }
        return original + " " + detected;
    }
    /**
     * DetectionResult结果实体，负责保存诊断结果数据。
     * 
     * @author zhang
     * @date 2026-06-29
     */

    public record DetectionResult(
            List<CommandSuggestion> commands,
            String riskLevel,
            List<String> warnings
    ) {
    }
}
