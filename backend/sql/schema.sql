-- MySQL dump 10.13  Distrib 8.4.0, for macos13.2 (arm64)
--
-- Host: localhost    Database: cosv_horizon
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `cosv_horizon`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `cosv_horizon` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `cosv_horizon`;

--
-- Table structure for table `api_key`
--

DROP TABLE IF EXISTS `api_key`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `api_key` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uuid` varchar(36) NOT NULL COMMENT 'API密钥的唯一公共标识符',
  `key_prefix` varchar(10) NOT NULL COMMENT '密钥前缀，用于识别',
  `key_hash` varchar(64) NOT NULL COMMENT '密钥的SHA-256哈希值',
  `creator_user_id` bigint NOT NULL COMMENT '创建密钥的用户ID',
  `organization_id` bigint DEFAULT NULL COMMENT '关联的组织ID，NULL表示个人密钥(PAT)',
  `description` varchar(255) DEFAULT NULL COMMENT '用户提供的密钥描述',
  `scopes` text COMMENT '授权范围列表，逗号分隔',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '密钥状态: ACTIVE, REVOKED, EXPIRED',
  `last_used_time` timestamp NULL DEFAULT NULL COMMENT '最后成功使用时间',
  `last_used_ip` varchar(45) DEFAULT NULL COMMENT '最后成功使用的IP地址',
  `expire_time` timestamp NULL DEFAULT NULL COMMENT '密钥过期时间，NULL表示永不过期',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uuid` (`uuid`),
  UNIQUE KEY `key_hash` (`key_hash`),
  KEY `idx_apikey_prefix` (`key_prefix`),
  KEY `idx_apikey_creator` (`creator_user_id`),
  KEY `idx_apikey_organization` (`organization_id`),
  KEY `idx_apikey_status` (`status`),
  KEY `idx_apikey_expire_time` (`expire_time`),
  KEY `idx_apikey_uuid` (`uuid`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='API密钥管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `api_key_usage_log`
--

DROP TABLE IF EXISTS `api_key_usage_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `api_key_usage_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uuid` varchar(36) NOT NULL COMMENT '日志记录的唯一标识符',
  `api_key_id` bigint NOT NULL COMMENT '关联的API密钥ID',
  `request_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '请求时间戳',
  `request_ip_address` varchar(45) NOT NULL COMMENT '请求来源IP地址',
  `request_method` varchar(10) NOT NULL COMMENT 'HTTP请求方法 (GET, POST, etc.)',
  `request_path` varchar(512) NOT NULL COMMENT '请求的API路径',
  `response_status_code` int NOT NULL COMMENT 'HTTP响应状态码',
  `user_agent` text COMMENT '请求的User-Agent头',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uuid` (`uuid`),
  KEY `idx_log_api_key_id` (`api_key_id`),
  KEY `idx_log_request_timestamp` (`request_timestamp`),
  KEY `idx_log_uuid` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='记录每一次API密钥的使用详情';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `app_setting`
--

DROP TABLE IF EXISTS `app_setting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `app_setting` (
  `key` varchar(128) NOT NULL,
  `value` text,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uuid` varchar(36) NOT NULL,
  `code` varchar(64) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` text,
  `create_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uuid` (`uuid`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_category_code` (`code`),
  KEY `idx_category_uuid` (`uuid`),
  KEY `idx_category_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cosv_file`
--

DROP TABLE IF EXISTS `cosv_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cosv_file` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uuid` varchar(36) NOT NULL,
  `identifier` varchar(255) NOT NULL,
  `modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `prev_cosv_file_id` bigint DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `schema_version` varchar(16) NOT NULL DEFAULT '1.0.0',
  `raw_cosv_file_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uuid` (`uuid`),
  KEY `idx_cosv_file_identifier` (`identifier`),
  KEY `idx_cosv_file_user_id` (`user_id`),
  KEY `idx_cosv_file_prev_file_id` (`prev_cosv_file_id`),
  KEY `idx_cosv_file_uuid` (`uuid`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cosv_generated_id`
--

DROP TABLE IF EXISTS `cosv_generated_id`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cosv_generated_id` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lnk_user_organization`
--

DROP TABLE IF EXISTS `lnk_user_organization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lnk_user_organization` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uuid` varchar(36) NOT NULL,
  `organization_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `role` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uuid` (`uuid`),
  UNIQUE KEY `organization_id` (`organization_id`,`user_id`),
  KEY `idx_lnk_user_organization_user_id` (`user_id`),
  KEY `idx_lnk_user_organization_organization_id` (`organization_id`),
  KEY `idx_lnk_user_organization_role` (`role`),
  KEY `idx_lnk_user_organization_compound` (`user_id`,`organization_id`),
  KEY `idx_lnk_user_organization_uuid` (`uuid`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lnk_vulnerability_metadata_tag`
--

DROP TABLE IF EXISTS `lnk_vulnerability_metadata_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lnk_vulnerability_metadata_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vulnerability_metadata_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `vulnerability_metadata_id` (`vulnerability_metadata_id`,`tag_id`),
  KEY `idx_lnk_vulnerability_metadata_tag_vuln_id` (`vulnerability_metadata_id`),
  KEY `idx_lnk_vulnerability_metadata_tag_tag_id` (`tag_id`),
  KEY `idx_lnk_vulnerability_metadata_tag_compound` (`vulnerability_metadata_id`,`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lnk_vulnerability_metadata_user`
--

DROP TABLE IF EXISTS `lnk_vulnerability_metadata_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lnk_vulnerability_metadata_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vulnerability_metadata_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `vulnerability_metadata_id` (`vulnerability_metadata_id`,`user_id`),
  KEY `idx_lnk_vulnerability_metadata_user_vuln_id` (`vulnerability_metadata_id`),
  KEY `idx_lnk_vulnerability_metadata_user_user_id` (`user_id`),
  KEY `idx_lnk_vulnerability_metadata_user_compound` (`vulnerability_metadata_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uuid` varchar(36) NOT NULL,
  `type` varchar(50) NOT NULL COMMENT '通知类型',
  `target_id` bigint DEFAULT NULL COMMENT '目标对象ID',
  `user_id` bigint NOT NULL COMMENT '接收者用户ID',
  `sender_id` bigint DEFAULT NULL COMMENT '发送者用户ID',
  `title` varchar(100) NOT NULL COMMENT '通知标题',
  `content` text NOT NULL COMMENT '通知内容',
  `is_read` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已读',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `expire_time` timestamp NULL DEFAULT NULL COMMENT '过期时间',
  `action_url` varchar(255) DEFAULT NULL COMMENT '操作链接',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '通知状态',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uuid` (`uuid`),
  KEY `idx_notification_user_id` (`user_id`),
  KEY `idx_notification_type` (`type`),
  KEY `idx_notification_target_id` (`target_id`),
  KEY `idx_notification_is_read` (`is_read`),
  KEY `idx_notification_create_time` (`create_time`),
  KEY `idx_notification_status` (`status`),
  KEY `idx_notification_uuid` (`uuid`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `org_invite_link`
--

DROP TABLE IF EXISTS `org_invite_link`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `org_invite_link` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uuid` varchar(64) NOT NULL,
  `org_id` bigint NOT NULL,
  `code` varchar(64) NOT NULL,
  `created_by` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expire_time` datetime DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uuid` (`uuid`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_org` (`org_id`),
  KEY `idx_active_expire` (`is_active`,`expire_time`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `org_points_ledger`
--

DROP TABLE IF EXISTS `org_points_ledger`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `org_points_ledger` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uuid` varchar(36) NOT NULL,
  `organization_id` bigint NOT NULL,
  `delta` int NOT NULL,
  `reason` varchar(100) NOT NULL,
  `ref_type` varchar(50) DEFAULT NULL,
  `ref_id` varchar(100) DEFAULT NULL,
  `idempotency_key` varchar(100) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uuid` (`uuid`),
  UNIQUE KEY `uniq_org_points_idem` (`organization_id`,`idempotency_key`),
  KEY `idx_org_points_org` (`organization_id`),
  KEY `idx_org_points_created` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='组织积分流水表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `organization`
--

DROP TABLE IF EXISTS `organization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `organization` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uuid` varchar(36) NOT NULL,
  `name` varchar(255) NOT NULL,
  `status` varchar(50) NOT NULL,
  `date_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `avatar` varchar(255) DEFAULT NULL,
  `description` text,
  `rating` bigint DEFAULT '0',
  `free_text` text,
  `is_verified` tinyint(1) NOT NULL DEFAULT '0',
  `reject_reason` text,
  `review_date` timestamp NULL DEFAULT NULL,
  `reviewed_by` bigint DEFAULT NULL,
  `is_public` tinyint(1) NOT NULL DEFAULT '1',
  `allow_join_request` tinyint(1) NOT NULL DEFAULT '0',
  `allow_invite_link` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uuid` (`uuid`),
  UNIQUE KEY `name` (`name`),
  KEY `idx_organization_name` (`name`),
  KEY `idx_organization_status` (`status`),
  KEY `idx_organization_uuid` (`uuid`),
  KEY `idx_org_public_status` (`is_public`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `original_login`
--

DROP TABLE IF EXISTS `original_login`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `original_login` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uuid` varchar(36) NOT NULL,
  `user_id` bigint NOT NULL,
  `source` varchar(50) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uuid` (`uuid`),
  UNIQUE KEY `source` (`source`,`name`),
  KEY `idx_original_login_user_id` (`user_id`),
  KEY `idx_original_login_uuid` (`uuid`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `raw_cosv_file`
--

DROP TABLE IF EXISTS `raw_cosv_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `raw_cosv_file` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uuid` varchar(36) NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `user_id` bigint NOT NULL,
  `organization_id` bigint DEFAULT NULL,
  `status` varchar(50) NOT NULL,
  `status_message` text,
  `content_length` bigint DEFAULT NULL,
  `create_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `storage_url` varchar(1024) DEFAULT NULL,
  `content` longblob,
  `checksum_sha256` char(64) DEFAULT NULL,
  `mime_type` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uuid` (`uuid`),
  KEY `idx_raw_cosv_file_user_id` (`user_id`),
  KEY `idx_raw_cosv_file_status` (`status`),
  KEY `idx_raw_cosv_file_organization_id` (`organization_id`),
  KEY `idx_raw_cosv_file_uuid` (`uuid`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tag`
--

DROP TABLE IF EXISTS `tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uuid` varchar(36) NOT NULL,
  `code` varchar(64) NOT NULL,
  `name` varchar(255) NOT NULL,
  `create_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uuid` (`uuid`),
  UNIQUE KEY `code` (`code`),
  UNIQUE KEY `name` (`name`),
  KEY `idx_tag_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uuid` varchar(36) NOT NULL,
  `name` varchar(255) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `role` varchar(50) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `company` varchar(255) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `git_hub` varchar(255) DEFAULT NULL,
  `status` varchar(50) NOT NULL,
  `rating` bigint DEFAULT '0',
  `website` varchar(255) DEFAULT NULL,
  `free_text` text,
  `real_name` varchar(255) DEFAULT NULL,
  `create_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uuid` (`uuid`),
  UNIQUE KEY `name` (`name`),
  UNIQUE KEY `idx_user_email` (`email`),
  KEY `idx_user_name` (`name`),
  KEY `idx_user_status` (`status`),
  KEY `idx_user_uuid` (`uuid`),
  CONSTRAINT `chk_user_role` CHECK ((`role` in (_utf8mb4'ADMIN',_utf8mb4'USER',_utf8mb4'MODERATOR'))),
  CONSTRAINT `chk_user_status` CHECK ((`status` in (_utf8mb4'CREATED',_utf8mb4'ACTIVE',_utf8mb4'INACTIVE',_utf8mb4'SUSPENDED')))
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_points_ledger`
--

DROP TABLE IF EXISTS `user_points_ledger`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_points_ledger` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uuid` varchar(36) NOT NULL,
  `user_id` bigint NOT NULL,
  `delta` int NOT NULL,
  `reason` varchar(100) NOT NULL,
  `ref_type` varchar(50) DEFAULT NULL,
  `ref_id` varchar(100) DEFAULT NULL,
  `idempotency_key` varchar(100) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uuid` (`uuid`),
  UNIQUE KEY `uniq_user_points_idem` (`user_id`,`idempotency_key`),
  KEY `idx_user_points_user` (`user_id`),
  KEY `idx_user_points_created` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户积分流水表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vulnerability_comment`
--

DROP TABLE IF EXISTS `vulnerability_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vulnerability_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uuid` varchar(36) NOT NULL,
  `vulnerability_metadata_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `parent_id` bigint DEFAULT NULL,
  `content` text NOT NULL,
  `is_edited` tinyint(1) NOT NULL DEFAULT '0',
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uuid` (`uuid`),
  KEY `idx_vc_vuln_id_ctime` (`vulnerability_metadata_id`,`create_time`),
  KEY `idx_vc_user_id` (`user_id`),
  KEY `idx_vc_parent_id` (`parent_id`),
  KEY `idx_vc_status` (`status`),
  KEY `idx_vc_uuid` (`uuid`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='漏洞评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vulnerability_metadata`
--

DROP TABLE IF EXISTS `vulnerability_metadata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vulnerability_metadata` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uuid` varchar(36) NOT NULL,
  `identifier` varchar(255) NOT NULL,
  `summary` varchar(255) NOT NULL,
  `details` text NOT NULL,
  `severity_num` float NOT NULL,
  `modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `submitted` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `published` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `withdrawn` timestamp NULL DEFAULT NULL,
  `language` varchar(50) NOT NULL,
  `status` varchar(50) NOT NULL,
  `user_id` bigint NOT NULL,
  `organization_id` bigint DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  `latest_cosv_file_id` bigint NOT NULL,
  `schema_version` varchar(16) NOT NULL DEFAULT '1.0.0',
  `review_date` timestamp NULL DEFAULT NULL,
  `reviewed_by` bigint DEFAULT NULL,
  `reject_reason` text,
  `confirmed_type` varchar(32) DEFAULT NULL,
  `database_specific` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uuid` (`uuid`),
  UNIQUE KEY `identifier` (`identifier`),
  KEY `idx_vulnerability_metadata_identifier` (`identifier`),
  KEY `idx_vulnerability_metadata_language` (`language`),
  KEY `idx_vulnerability_metadata_status` (`status`),
  KEY `idx_vulnerability_metadata_published` (`published`),
  KEY `idx_vulnerability_metadata_withdrawn` (`withdrawn`),
  KEY `idx_vulnerability_metadata_user_id` (`user_id`),
  KEY `idx_vulnerability_metadata_organization_id` (`organization_id`),
  KEY `idx_vulnerability_metadata_category_id` (`category_id`),
  KEY `idx_vulnerability_metadata_latest_file_id` (`latest_cosv_file_id`),
  KEY `idx_vulnerability_metadata_uuid` (`uuid`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vulnerability_metadata_affected_commit`
--

DROP TABLE IF EXISTS `vulnerability_metadata_affected_commit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vulnerability_metadata_affected_commit` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `package_id` bigint NOT NULL,
  `commit_type` varchar(16) NOT NULL,
  `commit_id` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_vm_pkg_commit_pid` (`package_id`),
  KEY `idx_vm_pkg_commit_type` (`commit_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vulnerability_metadata_affected_package`
--

DROP TABLE IF EXISTS `vulnerability_metadata_affected_package`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vulnerability_metadata_affected_package` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vulnerability_metadata_id` bigint NOT NULL,
  `ecosystem` varchar(64) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `purl` varchar(512) DEFAULT NULL,
  `language` varchar(64) DEFAULT NULL,
  `repository` varchar(1024) DEFAULT NULL,
  `home_page` varchar(1024) DEFAULT NULL,
  `edition` varchar(128) DEFAULT NULL,
  `ecosystem_specific` json DEFAULT NULL,
  `database_specific` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_vm_pkg_vmid` (`vulnerability_metadata_id`),
  KEY `idx_vm_pkg_purl` (`purl`(255)),
  KEY `idx_vm_pkg_eco` (`ecosystem`),
  KEY `idx_vm_pkg_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vulnerability_metadata_affected_range`
--

DROP TABLE IF EXISTS `vulnerability_metadata_affected_range`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vulnerability_metadata_affected_range` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `package_id` bigint NOT NULL,
  `type` varchar(16) NOT NULL,
  `repo` varchar(1024) DEFAULT NULL,
  `database_specific` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_vm_range_pid` (`package_id`),
  KEY `idx_vm_range_type` (`type`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vulnerability_metadata_affected_range_event`
--

DROP TABLE IF EXISTS `vulnerability_metadata_affected_range_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vulnerability_metadata_affected_range_event` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `range_id` bigint NOT NULL,
  `event_type` varchar(16) NOT NULL,
  `value` varchar(128) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `range_id` (`range_id`,`event_type`,`value`),
  KEY `idx_vm_range_event_rid` (`range_id`),
  KEY `idx_vm_range_event_type` (`event_type`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vulnerability_metadata_affected_version`
--

DROP TABLE IF EXISTS `vulnerability_metadata_affected_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vulnerability_metadata_affected_version` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `package_id` bigint NOT NULL,
  `version` varchar(128) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `package_id` (`package_id`,`version`),
  KEY `idx_vm_version_pid` (`package_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vulnerability_metadata_alias`
--

DROP TABLE IF EXISTS `vulnerability_metadata_alias`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vulnerability_metadata_alias` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vulnerability_metadata_id` bigint NOT NULL,
  `value` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `vulnerability_metadata_id` (`vulnerability_metadata_id`,`value`),
  UNIQUE KEY `uniq_vm_alias_value` (`value`),
  KEY `idx_vm_alias_vmid` (`vulnerability_metadata_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vulnerability_metadata_contributor`
--

DROP TABLE IF EXISTS `vulnerability_metadata_contributor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vulnerability_metadata_contributor` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vulnerability_metadata_id` bigint NOT NULL,
  `org` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `contributions` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_vm_contrib_vmid` (`vulnerability_metadata_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vulnerability_metadata_credit`
--

DROP TABLE IF EXISTS `vulnerability_metadata_credit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vulnerability_metadata_credit` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vulnerability_metadata_id` bigint NOT NULL,
  `name` varchar(255) NOT NULL,
  `type` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_vm_credit_vmid` (`vulnerability_metadata_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vulnerability_metadata_credit_contact`
--

DROP TABLE IF EXISTS `vulnerability_metadata_credit_contact`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vulnerability_metadata_credit_contact` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `credit_id` bigint NOT NULL,
  `contact` varchar(1024) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_vm_credit_contact_cid` (`credit_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vulnerability_metadata_cwe`
--

DROP TABLE IF EXISTS `vulnerability_metadata_cwe`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vulnerability_metadata_cwe` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vulnerability_metadata_id` bigint NOT NULL,
  `cwe_id` varchar(32) DEFAULT NULL,
  `cwe_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_vm_cwe_vmid` (`vulnerability_metadata_id`),
  KEY `idx_vm_cwe_id` (`cwe_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vulnerability_metadata_exploit_status`
--

DROP TABLE IF EXISTS `vulnerability_metadata_exploit_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vulnerability_metadata_exploit_status` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vulnerability_metadata_id` bigint NOT NULL,
  `status` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_vm_exploit_status_vmid` (`vulnerability_metadata_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vulnerability_metadata_patch_branch`
--

DROP TABLE IF EXISTS `vulnerability_metadata_patch_branch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vulnerability_metadata_patch_branch` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `patch_detail_id` bigint NOT NULL,
  `name` varchar(128) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_vm_patch_branch_pid` (`patch_detail_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vulnerability_metadata_patch_detail`
--

DROP TABLE IF EXISTS `vulnerability_metadata_patch_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vulnerability_metadata_patch_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vulnerability_metadata_id` bigint NOT NULL,
  `patch_url` varchar(1024) DEFAULT NULL,
  `issue_url` varchar(1024) DEFAULT NULL,
  `main_language` varchar(64) DEFAULT NULL,
  `author` varchar(255) DEFAULT NULL,
  `committer` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_vm_patch_vmid` (`vulnerability_metadata_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vulnerability_metadata_patch_tag`
--

DROP TABLE IF EXISTS `vulnerability_metadata_patch_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vulnerability_metadata_patch_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `patch_detail_id` bigint NOT NULL,
  `name` varchar(128) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_vm_patch_tag_pid` (`patch_detail_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vulnerability_metadata_project`
--

DROP TABLE IF EXISTS `vulnerability_metadata_project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vulnerability_metadata_project` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `uuid` varchar(36) NOT NULL,
  `name` varchar(255) NOT NULL,
  `url` varchar(255) NOT NULL,
  `versions` text NOT NULL,
  `vulnerability_metadata_id` bigint NOT NULL,
  `type` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uuid` (`uuid`),
  KEY `idx_vulnerability_metadata_project_type` (`type`),
  KEY `idx_vulnerability_metadata_project_vuln_id` (`vulnerability_metadata_id`),
  KEY `idx_vulnerability_metadata_project_uuid` (`uuid`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vulnerability_metadata_reference`
--

DROP TABLE IF EXISTS `vulnerability_metadata_reference`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vulnerability_metadata_reference` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vulnerability_metadata_id` bigint NOT NULL,
  `type` varchar(32) NOT NULL,
  `url` varchar(1024) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_vm_ref_vmid_url255` (`vulnerability_metadata_id`,`url`(255)),
  KEY `idx_vm_ref_type` (`type`),
  KEY `idx_vm_ref_url` (`url`(255))
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vulnerability_metadata_related`
--

DROP TABLE IF EXISTS `vulnerability_metadata_related`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vulnerability_metadata_related` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vulnerability_metadata_id` bigint NOT NULL,
  `value` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `vulnerability_metadata_id` (`vulnerability_metadata_id`,`value`),
  KEY `idx_vm_related_vmid` (`vulnerability_metadata_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vulnerability_metadata_severity`
--

DROP TABLE IF EXISTS `vulnerability_metadata_severity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vulnerability_metadata_severity` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vulnerability_metadata_id` bigint NOT NULL,
  `type` varchar(64) NOT NULL,
  `score` varchar(256) DEFAULT NULL,
  `level` varchar(32) DEFAULT NULL,
  `score_num` decimal(4,1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_vm_sev_vmid` (`vulnerability_metadata_id`),
  KEY `idx_vm_sev_type` (`type`),
  KEY `idx_vm_sev_level` (`level`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vulnerability_metadata_timeline`
--

DROP TABLE IF EXISTS `vulnerability_metadata_timeline`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vulnerability_metadata_timeline` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vulnerability_metadata_id` bigint NOT NULL,
  `type` varchar(32) NOT NULL,
  `value` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_vm_timeline_vmid` (`vulnerability_metadata_id`),
  KEY `idx_vm_timeline_type` (`type`),
  KEY `idx_vm_timeline_value` (`value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'cosv_horizon'
--

--
-- Dumping routines for database 'cosv_horizon'
--
/*!50003 DROP PROCEDURE IF EXISTS `hotfix_org_columns` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `hotfix_org_columns`()
BEGIN
    -- is_public
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'organization'
          AND COLUMN_NAME = 'is_public'
    ) THEN
        ALTER TABLE organization
            ADD COLUMN is_public TINYINT(1) NOT NULL DEFAULT 1 COMMENT '组织是否公开可见';
    END IF;

    -- allow_join_request
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'organization'
          AND COLUMN_NAME = 'allow_join_request'
    ) THEN
        ALTER TABLE organization
            ADD COLUMN allow_join_request TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否允许用户申请加入';
    END IF;

    -- allow_invite_link
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'organization'
          AND COLUMN_NAME = 'allow_invite_link'
    ) THEN
        ALTER TABLE organization
            ADD COLUMN allow_invite_link TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否允许通过邀请链接加入';
    END IF;

    -- is_verified (main fix)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'organization'
          AND COLUMN_NAME = 'is_verified'
    ) THEN
        ALTER TABLE organization
            ADD COLUMN is_verified TINYINT(1) NOT NULL DEFAULT 0 AFTER free_text;
    END IF;

END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-03  1:48:32
