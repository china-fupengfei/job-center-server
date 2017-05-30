DROP TABLE IF EXISTS `t_sched_job`;
CREATE TABLE `t_sched_job` (
  `id`                int(11)        NOT NULL AUTO_INCREMENT  COMMENT '自增主键ID',
  `name`              varchar(60)    NOT NULL                 COMMENT '名称',
  `cronExpression`    varchar(255)   NOT NULL                 COMMENT 'cron表达式',
  `handler`           text           NOT NULL                 COMMENT '任务处理类者（类全限定名或源代码）',
  `status`            smallint(4)    NOT NULL DEFAULT '0'     COMMENT '状态：0停止；1启动；',
  `concurrentSupport` tinyint(1)     NOT NULL DEFAULT '0'     COMMENT '是否支持并发执行：0不支持；1支持；',
  `recoverySupport`   tinyint(1)     NOT NULL DEFAULT '0'     COMMENT '是否支持恢复执行：0不支持；1支持；',
  `score`             smallint(4)    NOT NULL DEFAULT '1'     COMMENT '权重分数（分数越高则任务越重）',
  `execParams`        varchar(4000)  DEFAULT NULL             COMMENT '执行参数',
  `startTime`         datetime       DEFAULT NULL             COMMENT '任务开始时间（为空不限制）',
  `endTime`           datetime       DEFAULT NULL             COMMENT '任务结束时间（为空不限制）',
  `remark`            varchar(255)   DEFAULT NULL             COMMENT '备注',
  `isExecuting`       tinyint(1)     NOT NULL DEFAULT '0'     COMMENT '是否正在执行：0否；1是；',
  `execingTimeMillis` bigint(20)     DEFAULT NULL             COMMENT '本次执行时间（毫秒）',
  `lastSchedTime`     datetime       DEFAULT NULL             COMMENT '上一次的调度时间',
  `lastSchedServer`   varchar(128)   DEFAULT NULL             COMMENT '上一次的调度服务器IP',
  `nextSchedTime`     datetime       DEFAULT NULL             COMMENT '下一次的调度时间',
  `creatorId`         bigint(20)     NOT NULL                 COMMENT '创建人ID',
  `creatorName`       varchar(60)    NOT NULL                 COMMENT '创建人名称',
  `createTime`        datetime       NOT NULL                 COMMENT '创建时间',
  `modifierId`        bigint(20)     NOT NULL                 COMMENT '最近修改人ID',
  `modifierName`      varchar(60)    NOT NULL                 COMMENT '最近修改人姓名',
  `modifyTime`        datetime       NOT NULL                 COMMENT '修改时间',
  `version`           int(11)        NOT NULL DEFAULT '0'     COMMENT '版本号',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`),
  KEY `idx_modifyTime` (`modifyTime`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='调度任务表';


DROP TABLE IF EXISTS `t_sched_log`;
CREATE TABLE `t_sched_log` (
  `id`               bigint(20)     NOT NULL AUTO_INCREMENT  COMMENT '自增主键ID',
  `execServer`       varchar(128)   NOT NULL                 COMMENT '执行服务器IP',
  `isSuccess`        tinyint(1)     NOT NULL                 COMMENT '是否成功：0否；1是；',
  `isManualTrigger`  tinyint(1)     NOT NULL                 COMMENT '是否手动触发执行：0否；1是；',
  `jobId`            int(11)        NOT NULL                 COMMENT '任务ID',
  `jobName`          varchar(60)    NOT NULL                 COMMENT '任务名称',
  `execParams`       varchar(4000)  DEFAULT NULL             COMMENT '执行参数',
  `schedTime`        datetime       DEFAULT NULL             COMMENT '调度时间（为空表示手动触发执行）',
  `execStartTime`    datetime       NOT NULL                 COMMENT '执行开始时间',
  `execEndTime`      datetime       NOT NULL                 COMMENT '执行结束时间',
  `exception`        varchar(8000)  DEFAULT NULL             COMMENT '异常信息',
  `createTime`       datetime       NOT NULL                 COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_jobId` (`jobId`),
  KEY `idx_jobName` (`jobName`),
  KEY `idx_createTime` (`createTime`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='调度日志表';
