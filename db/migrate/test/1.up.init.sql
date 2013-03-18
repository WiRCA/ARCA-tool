SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

USE `wircatest` ;

-- -----------------------------------------------------
-- Table `wircatestuser`.`user`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `wircatestuser`.`user` (
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `email` VARCHAR(255) NOT NULL ,
  `name` VARCHAR(255) NOT NULL ,
  `password` VARCHAR(255) NOT NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `email` (`email` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `wircatest`.`rcacase`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `wircatest`.`rcacase` (
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `URLHash` varchar(255) DEFAULT NULL ,
  `importId` bigint(20) unsigned DEFAULT NULL ,
  `problemId` BIGINT(20) UNSIGNED NULL ,
  `ownerId` BIGINT(20) UNSIGNED NULL ,
  `name` VARCHAR(255) NOT NULL ,
  `companyName` VARCHAR(255) NOT NULL ,
  `caseTypeValue` SMALLINT(6) NOT NULL ,
  `companySizeValue` SMALLINT(6) NOT NULL ,
  `isMultinational` TINYINT(1) NULL DEFAULT '0' ,
  `isCasePublic` TINYINT(1) NULL DEFAULT '0' ,
  `caseGoals` TEXT NOT NULL ,
  `companyProducts` TEXT NOT NULL ,
  `description` TEXT NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_rcacase_1` (`problemId` ASC) ,
  INDEX `fk_rcacase_2` (`ownerId` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `wircatest`.`cause`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `wircatest`.`cause` (
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(255) NULL DEFAULT NULL ,
  `creatorId` BIGINT(20) UNSIGNED NULL ,
  `rcaCaseId` BIGINT(20) UNSIGNED NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_cause_1` (`creatorId` ASC) ,
  INDEX `fk_cause_2` (`rcaCaseId` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `wircatest`.`relation`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `wircatest`.`relation` (
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `causeFrom` BIGINT(20) UNSIGNED NOT NULL ,
  `causeTo` BIGINT(20) UNSIGNED NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_relation_1` (`causeFrom` ASC) ,
  INDEX `fk_relation_2` (`causeTo` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `wircatest`.`correction`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `wircatest`.`correction` (
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `causeId` BIGINT(20) UNSIGNED NOT NULL ,
  `name` VARCHAR(255) NOT NULL ,
  `description` TEXT NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_correction_1` (`causeId` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


USE `wircatestuser` ;


-- -----------------------------------------------------
-- Table `wircatestuser`.`usercases`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `wircatestuser`.`usercases` (
  `userId` BIGINT(20) UNSIGNED NOT NULL ,
  `caseId` BIGINT(20) UNSIGNED NULL ,
  PRIMARY KEY (`userId`, `caseId`) ,
  INDEX `fk_usercases_1` (`caseId` ASC) ,
  INDEX `fk_usercases_2` (`userId` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `wircatestuser`.`invitation`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `wircatestuser`.`invitation` (
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `hash` VARCHAR(255) NOT NULL ,
  `email` VARCHAR(255) NOT NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `hash_UNIQUE` (`hash` ASC) ,
  UNIQUE INDEX `email_UNIQUE` (`email` ASC) ,
  UNIQUE INDEX `id_UNIQUE` (`id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `wircatestuser`.`invitationCases`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `wircatestuser`.`invitationCases` (
  `invitationId` BIGINT(20) UNSIGNED NOT NULL ,
  `caseId` BIGINT(20) UNSIGNED NULL ,
  PRIMARY KEY (`invitationId`, `caseId`) ,
  INDEX `fk_invitationCases_1` (`invitationId` ASC) ,
  INDEX `fk_invitationCases_2` (`caseId` ASC) )
ENGINE = InnoDB;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
