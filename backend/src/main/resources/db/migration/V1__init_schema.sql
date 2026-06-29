CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(64) NOT NULL COMMENT '用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt密码哈希',
    nickname VARCHAR(64) COMMENT '昵称',
    email VARCHAR(128) COMMENT '邮箱',
    phone VARCHAR(32) COMMENT '手机号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
    last_login_at DATETIME COMMENT '最后登录时间',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0否，1是',
    UNIQUE KEY uk_sys_user_username (username),
    KEY idx_sys_user_status (status)
) COMMENT='用户表';

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(64) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) COMMENT '说明',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    UNIQUE KEY uk_sys_role_code (role_code)
) COMMENT='角色表';

CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    permission_code VARCHAR(128) NOT NULL COMMENT '权限编码',
    permission_name VARCHAR(128) NOT NULL COMMENT '权限名称',
    description VARCHAR(255) COMMENT '说明',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_sys_permission_code (permission_code)
) COMMENT='权限表';

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_sys_user_role (user_id, role_id),
    KEY idx_sys_user_role_role_id (role_id)
) COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_sys_role_permission (role_id, permission_id),
    KEY idx_sys_role_permission_permission_id (permission_id)
) COMMENT='角色权限关联表';

CREATE TABLE IF NOT EXISTS ops_diagnosis_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
    title VARCHAR(255) NOT NULL COMMENT '故障标题',
    category VARCHAR(64) COMMENT '故障类型',
    environment VARCHAR(255) COMMENT '运行环境',
    os_info VARCHAR(128) COMMENT '操作系统',
    middleware VARCHAR(128) COMMENT '中间件',
    service_type VARCHAR(128) COMMENT '服务类型',
    is_production TINYINT NOT NULL DEFAULT 0 COMMENT '是否生产环境',
    urgency_level VARCHAR(32) COMMENT '紧急程度',
    status VARCHAR(32) NOT NULL DEFAULT 'ANALYZING' COMMENT '会话状态',
    user_id BIGINT NOT NULL COMMENT '创建人ID',
    last_message_at DATETIME COMMENT '最后消息时间',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    KEY idx_ops_diag_session_user_time (user_id, created_at),
    KEY idx_ops_diag_session_category (category),
    KEY idx_ops_diag_session_status (status),
    KEY idx_ops_diag_session_prod (is_production)
) COMMENT='排障会话表';

CREATE TABLE IF NOT EXISTS ops_diagnosis_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    session_id BIGINT NOT NULL COMMENT '会话ID',
    role VARCHAR(32) NOT NULL COMMENT '角色：user/assistant/system',
    content LONGTEXT NOT NULL COMMENT '消息内容',
    content_sanitized LONGTEXT COMMENT '脱敏后的消息内容',
    token_count INT COMMENT '消息Token估算',
    created_by BIGINT COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_ops_diag_msg_session_time (session_id, created_at)
) COMMENT='排障消息表';

CREATE TABLE IF NOT EXISTS ops_diagnosis_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '结果ID',
    session_id BIGINT NOT NULL COMMENT '会话ID',
    message_id BIGINT COMMENT '关联AI消息ID',
    summary TEXT COMMENT '问题摘要',
    result_json JSON COMMENT '结构化分析结果',
    raw_response LONGTEXT COMMENT 'AI原始响应',
    risk_level VARCHAR(32) DEFAULT 'LOW' COMMENT '最高风险等级',
    need_restart TINYINT DEFAULT 0 COMMENT '是否需要重启',
    data_risk TINYINT DEFAULT 0 COMMENT '是否涉及数据风险',
    model_config_id BIGINT COMMENT '模型配置ID',
    prompt_template_id BIGINT COMMENT 'Prompt模板ID',
    prompt_version VARCHAR(32) COMMENT 'Prompt版本',
    created_by BIGINT COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_ops_diag_result_session (session_id),
    KEY idx_ops_diag_result_risk (risk_level)
) COMMENT='排障分析结果表';

CREATE TABLE IF NOT EXISTS ops_prompt_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    name VARCHAR(128) NOT NULL COMMENT '模板名称',
    category VARCHAR(64) COMMENT '适用故障类型',
    content LONGTEXT NOT NULL COMMENT '模板内容',
    version VARCHAR(32) NOT NULL DEFAULT '1.0.0' COMMENT '版本号',
    is_default TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认模板',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    KEY idx_ops_prompt_category (category),
    KEY idx_ops_prompt_status (status)
) COMMENT='Prompt模板表';

CREATE TABLE IF NOT EXISTS ops_model_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    provider VARCHAR(64) NOT NULL COMMENT '模型供应商',
    api_style VARCHAR(64) NOT NULL DEFAULT 'OPENAI_COMPATIBLE' COMMENT 'API风格',
    model_name VARCHAR(128) NOT NULL COMMENT '模型名称',
    api_base_url VARCHAR(255) COMMENT 'API地址',
    api_key_encrypted TEXT COMMENT '加密后的API Key',
    max_tokens INT NOT NULL DEFAULT 4096 COMMENT '最大Token',
    temperature DECIMAL(3,2) NOT NULL DEFAULT 0.30 COMMENT '温度参数',
    timeout_seconds INT NOT NULL DEFAULT 60 COMMENT '超时时间',
    default_model TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认模型',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    KEY idx_ops_model_provider (provider),
    KEY idx_ops_model_status (status),
    KEY idx_ops_model_default (default_model)
) COMMENT='模型配置表';

CREATE TABLE IF NOT EXISTS ops_ai_call_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '调用ID',
    request_id VARCHAR(64) COMMENT '请求ID',
    user_id BIGINT COMMENT '用户ID',
    session_id BIGINT COMMENT '会话ID',
    model_config_id BIGINT COMMENT '模型配置ID',
    provider VARCHAR(64) COMMENT '模型供应商',
    model_name VARCHAR(128) COMMENT '模型名称',
    prompt_tokens INT DEFAULT 0 COMMENT '输入Token',
    completion_tokens INT DEFAULT 0 COMMENT '输出Token',
    total_tokens INT DEFAULT 0 COMMENT '总Token',
    latency_ms INT COMMENT '耗时毫秒',
    cost DECIMAL(10,4) COMMENT '调用成本',
    success TINYINT NOT NULL COMMENT '是否成功',
    error_code VARCHAR(64) COMMENT '错误码',
    error_message TEXT COMMENT '错误信息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_ops_ai_log_user_time (user_id, created_at),
    KEY idx_ops_ai_log_session (session_id),
    KEY idx_ops_ai_log_success (success),
    KEY idx_ops_ai_log_request_id (request_id)
) COMMENT='AI调用日志表';

CREATE TABLE IF NOT EXISTS ops_risk_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '规则ID',
    rule_name VARCHAR(128) NOT NULL COMMENT '规则名称',
    pattern VARCHAR(512) NOT NULL COMMENT '匹配模式',
    pattern_type VARCHAR(32) NOT NULL DEFAULT 'KEYWORD' COMMENT 'KEYWORD/REGEX',
    risk_level VARCHAR(32) NOT NULL COMMENT '风险等级',
    warning_message VARCHAR(512) COMMENT '提示语',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    KEY idx_ops_risk_rule_level (risk_level),
    KEY idx_ops_risk_rule_status (status)
) COMMENT='风险命令规则表';

CREATE TABLE IF NOT EXISTS ops_case (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '案例ID',
    source_session_id BIGINT COMMENT '来源会话ID',
    title VARCHAR(255) NOT NULL COMMENT '案例标题',
    category VARCHAR(64) COMMENT '故障类型',
    environment VARCHAR(255) COMMENT '运行环境',
    symptom TEXT COMMENT '故障现象',
    log_content LONGTEXT COMMENT '原始日志',
    cause_analysis TEXT COMMENT '原因分析',
    solution LONGTEXT COMMENT '解决方案',
    prevention TEXT COMMENT '预防措施',
    commands JSON COMMENT '关联命令',
    tags JSON COMMENT '标签',
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '状态',
    reviewed_by BIGINT COMMENT '审核人',
    reviewed_at DATETIME COMMENT '审核时间',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    KEY idx_ops_case_category (category),
    KEY idx_ops_case_status (status),
    KEY idx_ops_case_created_by (created_by)
) COMMENT='案例库表';

CREATE TABLE IF NOT EXISTS ops_knowledge (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '知识ID',
    title VARCHAR(255) NOT NULL COMMENT '标题',
    category VARCHAR(64) COMMENT '分类',
    tags JSON COMMENT '标签',
    content LONGTEXT NOT NULL COMMENT '正文内容',
    content_type VARCHAR(32) NOT NULL DEFAULT 'MARKDOWN' COMMENT '内容类型',
    source_type VARCHAR(32) NOT NULL DEFAULT 'MANUAL' COMMENT '来源类型',
    source_ref VARCHAR(255) COMMENT '来源引用',
    version VARCHAR(32) NOT NULL DEFAULT '1.0.0' COMMENT '版本号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    KEY idx_ops_knowledge_category (category),
    KEY idx_ops_knowledge_status (status)
) COMMENT='知识库表';

CREATE TABLE IF NOT EXISTS ops_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报告ID',
    session_id BIGINT NOT NULL COMMENT '会话ID',
    title VARCHAR(255) NOT NULL COMMENT '报告标题',
    format VARCHAR(32) NOT NULL DEFAULT 'MARKDOWN' COMMENT '报告格式',
    content LONGTEXT NOT NULL COMMENT '报告内容',
    created_by BIGINT COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_ops_report_session (session_id),
    KEY idx_ops_report_created_by (created_by)
) COMMENT='复盘报告表';

CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(64) COMMENT '用户名',
    module VARCHAR(64) COMMENT '模块',
    action VARCHAR(64) COMMENT '操作',
    target_id BIGINT COMMENT '目标ID',
    request_method VARCHAR(16) COMMENT '请求方法',
    request_uri VARCHAR(255) COMMENT '请求URI',
    ip_address VARCHAR(64) COMMENT 'IP地址',
    user_agent VARCHAR(512) COMMENT 'User Agent',
    success TINYINT NOT NULL COMMENT '是否成功',
    error_message TEXT COMMENT '错误信息',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_sys_op_log_user_time (user_id, created_at),
    KEY idx_sys_op_log_module_action (module, action)
) COMMENT='系统操作日志表';

INSERT IGNORE INTO sys_user (id, username, password_hash, nickname, email, status, created_by, updated_by)
VALUES (1, 'admin', '$2b$12$Y2HU9tpxDm8mcQsdCnz3...2n3pOL3XzARv5u9lfX8SLugfYc4ySy', '管理员', 'admin@example.com', 1, 1, 1);

INSERT IGNORE INTO sys_role (id, role_code, role_name, description, status, created_by, updated_by)
VALUES
    (1, 'USER', '普通用户', '使用智能排障和查看自己的历史记录', 1, 1, 1),
    (2, 'OPS', '运维人员', '查看团队记录、保存案例、生成复盘', 1, 1, 1),
    (3, 'ADMIN', '管理员', '管理知识库、案例、模型配置', 1, 1, 1),
    (4, 'SUPER_ADMIN', '超级管理员', '拥有全部权限', 1, 1, 1);

INSERT IGNORE INTO sys_permission (id, permission_code, permission_name, description)
VALUES
    (1, 'diagnose:create', '发起排障', '发起智能排障分析'),
    (2, 'diagnose:view', '查看排障记录', '查看排障历史与详情'),
    (3, 'diagnose:delete', '删除排障记录', '删除排障历史记录'),
    (4, 'case:create', '新增案例', '创建案例'),
    (5, 'case:audit', '审核案例', '审核案例'),
    (6, 'case:publish', '发布案例', '发布或下架案例'),
    (7, 'kb:create', '新增知识', '创建知识库内容'),
    (8, 'kb:update', '修改知识', '修改知识库内容'),
    (9, 'kb:delete', '删除知识', '删除知识库内容'),
    (10, 'prompt:manage', 'Prompt管理', '管理Prompt模板'),
    (11, 'model:config', '模型配置', '管理模型配置'),
    (12, 'user:manage', '用户管理', '管理用户与角色');

INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (1, 4);

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 4, id FROM sys_permission;

INSERT IGNORE INTO ops_risk_rule (id, rule_name, pattern, pattern_type, risk_level, warning_message, status, created_by, updated_by)
VALUES
    (1, '递归删除', 'rm -rf', 'KEYWORD', 'HIGH', '该命令可能删除大量文件，请确认路径和备份。', 1, 1, 1),
    (2, '格式化文件系统', 'mkfs', 'KEYWORD', 'CRITICAL', '该命令会格式化文件系统，执行前必须确认磁盘和备份。', 1, 1, 1),
    (3, '底层磁盘写入', 'dd if=', 'KEYWORD', 'CRITICAL', '该命令可能覆盖磁盘数据，执行前必须确认目标设备。', 1, 1, 1),
    (4, '清空防火墙规则', 'iptables -F', 'KEYWORD', 'HIGH', '该命令会清空防火墙规则，可能影响生产网络访问控制。', 1, 1, 1),
    (5, '关闭防火墙', 'ufw disable', 'KEYWORD', 'HIGH', '该命令会关闭防火墙，可能暴露服务端口。', 1, 1, 1),
    (6, 'Docker清理', 'docker system prune', 'KEYWORD', 'HIGH', '该命令会清理Docker资源，可能删除未使用镜像、容器和网络。', 1, 1, 1),
    (7, '删除Docker卷', 'docker volume rm', 'KEYWORD', 'HIGH', '该命令可能删除持久化数据卷。', 1, 1, 1),
    (8, '删除K8s资源', 'kubectl delete', 'KEYWORD', 'HIGH', '该命令会删除Kubernetes资源，执行前请确认命名空间和对象。', 1, 1, 1),
    (9, '删除数据库', 'DROP DATABASE', 'KEYWORD', 'CRITICAL', '该命令会删除整个数据库，执行前必须确认备份。', 1, 1, 1),
    (10, '清空数据表', 'TRUNCATE TABLE', 'KEYWORD', 'CRITICAL', '该命令会清空表数据，执行前必须确认备份。', 1, 1, 1);

