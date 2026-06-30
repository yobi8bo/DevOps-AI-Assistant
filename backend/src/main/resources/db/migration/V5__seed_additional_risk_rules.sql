INSERT IGNORE INTO ops_risk_rule (id, rule_name, pattern, pattern_type, risk_level, warning_message, status, created_by, updated_by)
VALUES
    (11, '强制删除文件', 'rm -f', 'KEYWORD', 'HIGH', '该命令可能删除文件，请确认目标路径、备份和影响范围。', 1, 1, 1),
    (12, '删除目录', 'rmdir', 'KEYWORD', 'MEDIUM', '该命令会删除目录，请确认目录为空且不影响业务。', 1, 1, 1),
    (13, '重启服务', 'systemctl restart', 'KEYWORD', 'HIGH', '该命令会重启服务，可能影响生产流量，请确认窗口期和回滚方案。', 1, 1, 1),
    (14, '停止服务', 'systemctl stop', 'KEYWORD', 'HIGH', '该命令会停止服务，可能造成业务不可用，请确认影响范围。', 1, 1, 1),
    (15, '重启主机', 'reboot', 'KEYWORD', 'CRITICAL', '该命令会重启主机，可能中断生产服务，请确认维护窗口。', 1, 1, 1),
    (16, '关机', 'shutdown', 'KEYWORD', 'CRITICAL', '该命令会关闭或重启主机，请确认维护窗口和业务影响。', 1, 1, 1),
    (17, '修改防火墙规则', 'iptables', 'KEYWORD', 'HIGH', '该命令会修改防火墙规则，可能影响网络访问，请确认规则和回滚方案。', 1, 1, 1),
    (18, '删除数据库表', 'DROP TABLE', 'KEYWORD', 'CRITICAL', '该命令会删除数据表，执行前必须确认备份和恢复方案。', 1, 1, 1),
    (19, '删除数据', 'DELETE FROM', 'KEYWORD', 'HIGH', '该命令会删除数据库数据，请确认 WHERE 条件、备份和事务回滚方案。', 1, 1, 1),
    (20, '更新数据', 'UPDATE ', 'KEYWORD', 'MEDIUM', '该命令会修改数据库数据，请确认 WHERE 条件和回滚方案。', 1, 1, 1),
    (21, '修改文件权限', 'chmod 777', 'KEYWORD', 'HIGH', '该命令会放开文件权限，可能造成安全风险，请确认最小权限原则。', 1, 1, 1),
    (22, '修改文件归属', 'chown -R', 'KEYWORD', 'MEDIUM', '该命令会递归修改文件归属，请确认目标路径和影响范围。', 1, 1, 1);
