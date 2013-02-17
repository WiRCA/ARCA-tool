-- Migrate script for migrating WiRCA 1.0 database to WiRCA 1.1
-- Jari Jaanto

ALTER TABLE `rcacase` ADD COLUMN `URLHash` varchar(255) DEFAULT NULL;
ALTER TABLE `rcacase` ADD COLUMN `importId` bigint(20) unsigned DEFAULT NULL;


CREATE TABLE `classification` (
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

CREATE TABLE `classificationpair` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `child_id` bigint(20) unsigned DEFAULT NULL,
  `parent_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY  (`child_id`),
  KEY  (`parent_id`),
  CONSTRAINT  FOREIGN KEY (`child_id`) REFERENCES `classification` (`id`),
  CONSTRAINT  FOREIGN KEY (`parent_id`) REFERENCES `classification` (`id`)
);
CREATE TABLE `cause_classificationpair` (
  `cause_id` bigint(20) unsigned NOT NULL,
  `classifications_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`cause_id`,`classifications_id`),
  KEY  (`cause_id`),
  KEY  (`classifications_id`),
  CONSTRAINT FOREIGN KEY (`classifications_id`) REFERENCES `classificationpair` (`id`),
  CONSTRAINT FOREIGN KEY (`cause_id`) REFERENCES `cause` (`id`)
);

CREATE TABLE `dimension` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `dimensionId` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);
