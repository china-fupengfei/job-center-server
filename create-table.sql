DROP TABLE IF EXISTS `t_sched_job`;
CREATE TABLE `t_sched_job` (
  `id`                   int(11)        NOT NULL AUTO_INCREMENT  COMMENT '自增主键ID',
  `name`                 varchar(60)    NOT NULL                 COMMENT '名称',
  `cron_expression`      varchar(255)   NOT NULL                 COMMENT 'cron表达式',
  `handler`              text           NOT NULL                 COMMENT '任务处理类者（类全限定名或源代码）',
  `status`               smallint(4)    NOT NULL DEFAULT '0'     COMMENT '状态：0停止；1启动；',
  `concurrent_support`   tinyint(1)     NOT NULL DEFAULT '0'     COMMENT '是否支持并发执行：0不支持；1支持；',
  `recovery_support`     tinyint(1)     NOT NULL DEFAULT '0'     COMMENT '是否支持恢复执行：0不支持；1支持；',
  `score`                smallint(4)    NOT NULL DEFAULT '1'     COMMENT '权重分数（分数越高则表示任务越重）',
  `exec_params`          varchar(4000)  DEFAULT NULL             COMMENT '执行参数',
  `start_time`           datetime       DEFAULT NULL             COMMENT '任务开始时间（为空不限制）',
  `end_time`             datetime       DEFAULT NULL             COMMENT '任务结束时间（为空不限制）',
  `remark`               varchar(255)   DEFAULT NULL             COMMENT '备注',
  `is_executing`         tinyint(1)     NOT NULL DEFAULT '0'     COMMENT '是否正在执行：0否；1是；',
  `execing_time_millis`  bigint(20)     DEFAULT NULL             COMMENT '本次执行时间（毫秒）',
  `last_sched_time`      datetime       DEFAULT NULL             COMMENT '上一次的调度时间',
  `last_sched_server`    varchar(128)   DEFAULT NULL             COMMENT '上一次的调度服务器IP',
  `next_sched_time`      datetime       DEFAULT NULL             COMMENT '下一次的调度时间',
  `creator_id`           bigint(20)     NOT NULL                 COMMENT '创建人ID',
  `creator_name`         varchar(60)    NOT NULL                 COMMENT '创建人名称',
  `create_time`          datetime       NOT NULL                 COMMENT '创建时间',
  `modifier_id`          bigint(20)     NOT NULL                 COMMENT '最近修改人ID',
  `modifier_name`        varchar(60)    NOT NULL                 COMMENT '最近修改人姓名',
  `modify_time`          datetime       NOT NULL                 COMMENT '修改时间',
  `version`              int(11)        NOT NULL DEFAULT '0'     COMMENT '版本号',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`),
  KEY `idx_modifytime` (`modify_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='调度任务表';


DROP TABLE IF EXISTS `t_sched_log`;
CREATE TABLE `t_sched_log` (
  `id`                 bigint(20)     NOT NULL AUTO_INCREMENT  COMMENT '自增主键ID',
  `exec_server`        varchar(128)   NOT NULL                 COMMENT '执行服务器IP',
  `is_success`         tinyint(1)     NOT NULL                 COMMENT '是否成功：0否；1是；',
  `is_manual_trigger`  tinyint(1)     NOT NULL                 COMMENT '是否手动触发执行：0否；1是；',
  `job_id`             int(11)        NOT NULL                 COMMENT '任务ID',
  `job_name`           varchar(60)    NOT NULL                 COMMENT '任务名称',
  `exec_params`        varchar(4000)  DEFAULT NULL             COMMENT '执行参数',
  `sched_time`         datetime       DEFAULT NULL             COMMENT '调度时间（为空表示手动触发执行）',
  `exec_start_time`    datetime       NOT NULL                 COMMENT '执行开始时间',
  `exec_end_time`      datetime       NOT NULL                 COMMENT '执行结束时间',
  `exception`          varchar(8000)  DEFAULT NULL             COMMENT '异常信息',
  `create_time`        datetime       NOT NULL                 COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_jobid` (`job_id`),
  KEY `idx_jobname` (`job_name`),
  KEY `idx_createtime` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='调度日志表';
