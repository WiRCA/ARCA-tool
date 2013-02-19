SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';


USE `arcatool` ;

ALTER TABLE `rcacase` ADD COLUMN `URLHash` varchar(255) DEFAULT NULL;
ALTER TABLE `rcacase` ADD COLUMN `importId` bigint(20) unsigned DEFAULT NULL;
UPDATE rcacase SET URLHash = UUID();

CREATE TABLE IF NOT EXISTS `classification` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `abbreviation` varchar(255) DEFAULT NULL,
  `classificationDimension` int(11) NOT NULL,
  `creatorId` bigint(20) DEFAULT NULL,
  `explanation` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `updated` datetime NOT NULL,
  `cause_id` bigint(20) unsigned DEFAULT NULL,
  `rcaCaseId` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY  (`cause_id`),
  KEY  (`rcaCaseId`),
  CONSTRAINT  FOREIGN KEY (`cause_id`) REFERENCES `cause` (`id`),
  CONSTRAINT  FOREIGN KEY (`rcaCaseId`) REFERENCES `rcacase` (`id`)
);

CREATE TABLE IF NOT EXISTS `classificationpair` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `child_id` bigint(20) unsigned DEFAULT NULL,
  `parent_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY  (`child_id`),
  KEY  (`parent_id`),
  CONSTRAINT  FOREIGN KEY (`child_id`) REFERENCES `classification` (`id`),
  CONSTRAINT  FOREIGN KEY (`parent_id`) REFERENCES `classification` (`id`)
);

CREATE TABLE IF NOT EXISTS `cause_classificationpair` (
  `cause_id` bigint(20) unsigned NOT NULL,
  `classifications_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`cause_id`,`classifications_id`),
  KEY  (`cause_id`),
  KEY  (`classifications_id`),
  CONSTRAINT FOREIGN KEY (`classifications_id`) REFERENCES `classificationpair` (`id`),
  CONSTRAINT FOREIGN KEY (`cause_id`) REFERENCES `cause` (`id`)
);

CREATE TABLE IF NOT EXISTS `dimension` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `dimensionId` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

INSERT INTO dimension (dimensionId, name) VALUES (1, 'What');
INSERT INTO dimension (dimensionId, name) VALUES (2, 'Where');
