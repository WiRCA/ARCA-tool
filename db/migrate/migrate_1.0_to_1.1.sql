-- Migrate script for migrating WiRCA 1.0 database to WiRCA 1.1
-- Jari Jaanto

ALTER TABLE `rcacase` ADD COLUMN `URLHash` varchar(255) DEFAULT NULL;
ALTER TABLE `rcacase` ADD COLUMN `importId` bigint(20) DEFAULT NULL;

CREATE TABLE `cause_classificationpair` (
  `cause_id` bigint(20) NOT NULL,
  `classifications_id` bigint(20) NOT NULL,
  PRIMARY KEY (`cause_id`,`classifications_id`),
  KEY `FKE7A3F356AFC11C76` (`cause_id`),
  KEY `FKE7A3F356569B9751` (`classifications_id`),
  CONSTRAINT `FKE7A3F356569B9751` FOREIGN KEY (`classifications_id`) REFERENCES `classificationpair` (`id`),
  CONSTRAINT `FKE7A3F356AFC11C76` FOREIGN KEY (`cause_id`) REFERENCES `cause` (`id`)
);

CREATE TABLE `classification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `abbreviation` varchar(255) DEFAULT NULL,
  `classificationDimension` int(11) NOT NULL,
  `creatorId` bigint(20) DEFAULT NULL,
  `explanation` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `updated` datetime NOT NULL,
  `cause_id` bigint(20) DEFAULT NULL,
  `rcaCaseId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK16CA33E6AFC11C76` (`cause_id`),
  KEY `FK16CA33E6FE1BBC97` (`rcaCaseId`),
  CONSTRAINT `FK16CA33E6AFC11C76` FOREIGN KEY (`cause_id`) REFERENCES `cause` (`id`),
  CONSTRAINT `FK16CA33E6FE1BBC97` FOREIGN KEY (`rcaCaseId`) REFERENCES `rcacase` (`id`)
);

CREATE TABLE `classificationpair` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `child_id` bigint(20) DEFAULT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7D5633C070016328` (`child_id`),
  KEY `FK7D5633C0887DFCDA` (`parent_id`),
  CONSTRAINT `FK7D5633C070016328` FOREIGN KEY (`child_id`) REFERENCES `classification` (`id`),
  CONSTRAINT `FK7D5633C0887DFCDA` FOREIGN KEY (`parent_id`) REFERENCES `classification` (`id`)
);

CREATE TABLE `dimension` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `dimensionId` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `invitation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `hash` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `invitationCases` (
  `invitationId` bigint(20) NOT NULL,
  `caseId` bigint(20) DEFAULT NULL
);

CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `usercases` (
  `userId` bigint(20) NOT NULL,
  `caseId` bigint(20) NOT NULL,
  PRIMARY KEY (`userId`,`caseId`)
);