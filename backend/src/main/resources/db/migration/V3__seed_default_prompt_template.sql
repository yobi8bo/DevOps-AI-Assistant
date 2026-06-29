INSERT INTO ops_prompt_template (
    name,
    category,
    content,
    version,
    is_default,
    status,
    created_by,
    updated_by
)
SELECT
    '通用运维排障模板',
    NULL,
    '请基于以下运维故障信息进行诊断，并返回结构化 JSON。

分析要求：
1. 结论必须谨慎，不能编造用户没有提供的环境事实。
2. 命令建议优先使用只读检查命令。
3. 涉及删除、格式化、重启、清理、防火墙变更、数据库写操作时必须标记 HIGH 或 CRITICAL，并写明风险提示。
4. 如果信息不足，把需要补充的信息放入 needMoreInfo。
5. riskLevel 只能是 LOW、MEDIUM、HIGH、CRITICAL。

标题：{{title}}
故障类型：{{category}}
运行环境：{{environment}}
操作系统：{{osInfo}}
中间件：{{middleware}}
服务类型：{{serviceType}}
是否生产环境：{{isProduction}}
紧急程度：{{urgencyLevel}}

故障描述：
{{description}}

日志内容：
{{logContent}}

命令输出：
{{commandOutput}}',
    '1.0.0',
    1,
    1,
    1,
    1
WHERE NOT EXISTS (
    SELECT 1 FROM ops_prompt_template WHERE is_default = 1 AND deleted = 0
);
