-- 目的：将漏洞别名（例如 CVE 编号）在全局范围内设为唯一，避免同一别名关联到多个漏洞。
-- 使用前请先清理历史冲突数据，建议步骤如下：
-- 1) 查询重复别名：
--    SELECT value, COUNT(*) AS cnt, GROUP_CONCAT(vulnerability_metadata_id) AS vm_ids
--    FROM vulnerability_metadata_alias
--    GROUP BY value HAVING COUNT(*) > 1;
-- 2) 对重复项进行人工/脚本处理（保留权威关联，删除其余别名）。
-- 3) 确认无重复后，再执行下面的唯一索引创建语句。

-- 添加全局唯一索引（大小写敏感/不敏感取决于列的字符集排序规则，一般 utf8mb4_* 为不区分大小写）
ALTER TABLE vulnerability_metadata_alias
  ADD UNIQUE INDEX uniq_vm_alias_value (value);

-- 回滚参考（如需移除唯一性约束）：
-- ALTER TABLE vulnerability_metadata_alias DROP INDEX uniq_vm_alias_value;

