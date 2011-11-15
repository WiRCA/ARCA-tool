SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

ALTER SCHEMA `wircatest`  DEFAULT CHARACTER SET utf8  DEFAULT COLLATE utf8_general_ci ;

USE `wircatest`;

ALTER TABLE `wircatest`.`cause` CHARACTER SET = utf8 , COLLATE = utf8_general_ci , ENGINE = InnoDB , CHANGE COLUMN `creator_id` `creatorId` BIGINT(20) UNSIGNED NOT NULL  , CHANGE COLUMN `rcacase_id` `rcaCaseId` BIGINT(20) UNSIGNED NOT NULL  
, DROP INDEX `creator_id` 
, ADD INDEX `creator_id` (`creatorId` ASC) 
, DROP INDEX `rcacase_id` 
, ADD INDEX `rcacase_id` (`rcaCaseId` ASC) 
, ADD INDEX `fk_cause_1` (`creatorId` ASC) 
, ADD INDEX `fk_cause_2` (`rcaCaseId` ASC) ;

ALTER TABLE `wircatest`.`causesof` CHARACTER SET = utf8 , COLLATE = utf8_general_ci , ENGINE = InnoDB , ADD COLUMN `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT  FIRST , CHANGE COLUMN `cause_id` `causeFrom` BIGINT(20) UNSIGNED NOT NULL  , CHANGE COLUMN `effect_id` `causeTo` BIGINT(20) UNSIGNED NOT NULL  
, DROP PRIMARY KEY 
, ADD PRIMARY KEY (`id`) 
, DROP INDEX `effect_id` 
, ADD INDEX `effect_id` (`causeTo` ASC) 
, ADD INDEX `fk_relation_1` (`causeFrom` ASC) 
, ADD INDEX `fk_relation_2` (`causeTo` ASC) , RENAME TO  `wircatest`.`relation` ;

ALTER TABLE `wircatest`.`corrections` CHARACTER SET = utf8 , COLLATE = utf8_general_ci , ENGINE = InnoDB , ADD COLUMN `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT  FIRST , ADD COLUMN `description` TEXT NOT NULL  AFTER `name` , CHANGE COLUMN `cause_id` `causeId` BIGINT(20) UNSIGNED NOT NULL  , CHANGE COLUMN `correction` `name` VARCHAR(255) NOT NULL DEFAULT ''  
, DROP PRIMARY KEY 
, ADD PRIMARY KEY (`id`) 
, ADD INDEX `fk_correction_1` (`causeId` ASC) , RENAME TO  `wircatest`.`correction` ;

ALTER TABLE `wircatest`.`patchlevel` CHARACTER SET = utf8 , COLLATE = utf8_general_ci , ENGINE = InnoDB ;

ALTER TABLE `wircatest`.`rcacase` CHARACTER SET = utf8 , COLLATE = utf8_general_ci , ENGINE = InnoDB , ADD COLUMN `caseGoals` TEXT NOT NULL  AFTER `isCasePublic` , ADD COLUMN `companyProducts` TEXT NOT NULL  AFTER `caseGoals` , ADD COLUMN `description` TEXT NOT NULL  AFTER `companyProducts` , CHANGE COLUMN `name` `name` VARCHAR(255) NOT NULL  AFTER `ownerId` , CHANGE COLUMN `problem_id` `problem` BIGINT(20) UNSIGNED NOT NULL  , CHANGE COLUMN `owner_id` `ownerId` BIGINT(20) UNSIGNED NOT NULL  , CHANGE COLUMN `company_name` `companyName` VARCHAR(255) NOT NULL  , CHANGE COLUMN `case_type_value` `caseTypeValue` SMALLINT(6) NOT NULL  , CHANGE COLUMN `company_size_value` `companySizeValue` SMALLINT(6) NOT NULL  , CHANGE COLUMN `is_multinational` `isMultinational` TINYINT(1) NULL DEFAULT '0'  , CHANGE COLUMN `is_case_public` `isCasePublic` TINYINT(1) NULL DEFAULT '0'  
, DROP INDEX `problem_id` 
, ADD INDEX `problem_id` (`problem` ASC) 
, DROP INDEX `owner_id` 
, ADD INDEX `owner_id` (`ownerId` ASC) 
, ADD INDEX `fk_rcacase_1` (`problem` ASC) 
, ADD INDEX `fk_rcacase_2` (`ownerId` ASC) ;

ALTER SCHEMA `wircatestuser`  DEFAULT CHARACTER SET utf8  DEFAULT COLLATE utf8_general_ci ;

USE `wircatestuser`;

ALTER TABLE `wircatestuser`.`patchlevel` CHARACTER SET = utf8 , COLLATE = utf8_general_ci , ENGINE = InnoDB ;

ALTER TABLE `wircatestuser`.`user` CHARACTER SET = utf8 , COLLATE = utf8_general_ci , ENGINE = InnoDB , CHANGE COLUMN `name` `name` VARCHAR(255) NOT NULL  ;

ALTER TABLE `wircatestuser`.`usercases` CHARACTER SET = utf8 , COLLATE = utf8_general_ci , ENGINE = InnoDB , CHANGE COLUMN `user_id` `userId` BIGINT(20) UNSIGNED NOT NULL  , CHANGE COLUMN `case_id` `caseId` BIGINT(20) UNSIGNED NOT NULL  
, DROP PRIMARY KEY 
, ADD PRIMARY KEY (`userId`, `caseId`) 
, ADD INDEX `fk_usercases_1` (`caseId` ASC) 
, ADD INDEX `fk_usercases_2` (`userId` ASC) ;

CREATE  TABLE IF NOT EXISTS `wircatestuser`.`invitation` (
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `hash` VARCHAR(255) NOT NULL ,
  `email` VARCHAR(255) NOT NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `hash_UNIQUE` (`hash` ASC) ,
  UNIQUE INDEX `email_UNIQUE` (`email` ASC) ,
  UNIQUE INDEX `id_UNIQUE` (`id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci;

CREATE  TABLE IF NOT EXISTS `wircatestuser`.`invitationCases` (
  `invitationId` BIGINT(20) UNSIGNED NOT NULL ,
  `caseId` BIGINT(20) UNSIGNED NOT NULL ,
  PRIMARY KEY (`invitationId`, `caseId`) ,
  INDEX `fk_invitationCases_1` (`invitationId` ASC) ,
  INDEX `fk_invitationCases_2` (`caseId` ASC) ,
  CONSTRAINT `fk_invitationCases_1`
    FOREIGN KEY (`invitationId` )
    REFERENCES `wircatestuser`.`invitation` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_invitationCases_2`
    FOREIGN KEY (`caseId` )
    REFERENCES `wircatest`.`rcacase` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_general_ci;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
