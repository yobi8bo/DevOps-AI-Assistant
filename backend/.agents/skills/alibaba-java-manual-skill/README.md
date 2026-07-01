# alibaba-java-development-manual Skill

这是由用户提供的《阿里巴巴Java开发规范（嵩山版）.pdf》转换而来的 Claude/Codex 可用 Skill。

## 内容

- `SKILL.md`：Skill 入口文件，适合 Claude/Codex 读取。
- `references/manual-full-text.md`：PDF 全文文本转写，按页码组织。
- `references/rule-index.md`：规约索引，便于快速定位规则。
- `references/阿里巴巴Java开发规范（嵩山版）.pdf`：原始 PDF，便于核对。
- `checklists/review-checklist.md`：审查输出清单。

## 使用方式

将整个 `alibaba-java-manual-skill` 文件夹复制到你使用的 Agent Skills 目录中即可。不同工具的目录位置可能不同；核心要求是让工具能读取该文件夹下的 `SKILL.md`。

## 注意

本 Skill 的目标是“按原文辅助使用”，不是重新创作一套 Java 规范。精确判断以原始 PDF 和全文转写为准。
