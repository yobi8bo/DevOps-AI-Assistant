CREATE TABLE IF NOT EXISTS ops_fault_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    category_code VARCHAR(64) NOT NULL COMMENT '分类编码',
    category_name VARCHAR(64) NOT NULL COMMENT '分类名称',
    description VARCHAR(255) COMMENT '说明',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    UNIQUE KEY uk_ops_fault_category_code (category_code),
    KEY idx_ops_fault_category_status_sort (status, sort_order)
) COMMENT='故障分类表';

INSERT INTO ops_fault_category (category_code, category_name, description, sort_order, status)
SELECT 'LINUX', 'Linux', 'Linux 系统类故障', 10, 1
WHERE NOT EXISTS (SELECT 1 FROM ops_fault_category WHERE category_code = 'LINUX');

INSERT INTO ops_fault_category (category_code, category_name, description, sort_order, status)
SELECT 'DOCKER', 'Docker', 'Docker 容器类故障', 20, 1
WHERE NOT EXISTS (SELECT 1 FROM ops_fault_category WHERE category_code = 'DOCKER');

INSERT INTO ops_fault_category (category_code, category_name, description, sort_order, status)
SELECT 'KUBERNETES', 'Kubernetes', 'Kubernetes 集群类故障', 30, 1
WHERE NOT EXISTS (SELECT 1 FROM ops_fault_category WHERE category_code = 'KUBERNETES');

INSERT INTO ops_fault_category (category_code, category_name, description, sort_order, status)
SELECT 'JENKINS', 'Jenkins', 'CI/CD 构建发布类故障', 40, 1
WHERE NOT EXISTS (SELECT 1 FROM ops_fault_category WHERE category_code = 'JENKINS');

INSERT INTO ops_fault_category (category_code, category_name, description, sort_order, status)
SELECT 'NGINX', 'Nginx', 'Nginx 网关代理类故障', 50, 1
WHERE NOT EXISTS (SELECT 1 FROM ops_fault_category WHERE category_code = 'NGINX');

INSERT INTO ops_fault_category (category_code, category_name, description, sort_order, status)
SELECT 'MYSQL', 'MySQL', 'MySQL 数据库类故障', 60, 1
WHERE NOT EXISTS (SELECT 1 FROM ops_fault_category WHERE category_code = 'MYSQL');

INSERT INTO ops_fault_category (category_code, category_name, description, sort_order, status)
SELECT 'REDIS', 'Redis', 'Redis 缓存类故障', 70, 1
WHERE NOT EXISTS (SELECT 1 FROM ops_fault_category WHERE category_code = 'REDIS');

INSERT INTO ops_fault_category (category_code, category_name, description, sort_order, status)
SELECT 'SPRING_BOOT', 'Spring Boot', 'Spring Boot 应用类故障', 80, 1
WHERE NOT EXISTS (SELECT 1 FROM ops_fault_category WHERE category_code = 'SPRING_BOOT');

INSERT INTO ops_fault_category (category_code, category_name, description, sort_order, status)
SELECT 'JAVA', 'Java', 'Java 运行时类故障', 90, 1
WHERE NOT EXISTS (SELECT 1 FROM ops_fault_category WHERE category_code = 'JAVA');

INSERT INTO ops_fault_category (category_code, category_name, description, sort_order, status)
SELECT 'NETWORK', '网络', '网络连通性和 DNS 类故障', 100, 1
WHERE NOT EXISTS (SELECT 1 FROM ops_fault_category WHERE category_code = 'NETWORK');

INSERT INTO ops_fault_category (category_code, category_name, description, sort_order, status)
SELECT 'PERMISSION', '权限', '权限和认证授权类故障', 110, 1
WHERE NOT EXISTS (SELECT 1 FROM ops_fault_category WHERE category_code = 'PERMISSION');

INSERT INTO ops_fault_category (category_code, category_name, description, sort_order, status)
SELECT 'DISK', '磁盘', '磁盘容量和 IO 类故障', 120, 1
WHERE NOT EXISTS (SELECT 1 FROM ops_fault_category WHERE category_code = 'DISK');

INSERT INTO ops_fault_category (category_code, category_name, description, sort_order, status)
SELECT 'CPU_MEMORY', 'CPU / 内存', 'CPU 和内存资源类故障', 130, 1
WHERE NOT EXISTS (SELECT 1 FROM ops_fault_category WHERE category_code = 'CPU_MEMORY');

INSERT INTO ops_fault_category (category_code, category_name, description, sort_order, status)
SELECT 'OTHER', '其他', '未归类故障', 999, 1
WHERE NOT EXISTS (SELECT 1 FROM ops_fault_category WHERE category_code = 'OTHER');
