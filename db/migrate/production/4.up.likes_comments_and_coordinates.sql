SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';


USE `arcatool` ;

-- -----------------------------------------------------
-- Table `arcatool`.`causelikes`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `arcatool`.`causelikes` (
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `causeId` BIGINT(20) UNSIGNED NOT NULL,
  `userId` BIGINT(20) UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_causelikes_causeId` (`causeId` ASC),
  INDEX `fk_causelikes_userId` (`userId` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Table `arcatool`.`correctionlikes`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `arcatool`.`correctionlikes` (
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `correctionId` BIGINT(20) UNSIGNED NOT NULL,
  `userId` BIGINT(20) UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_correctionlikes_correctionId` (`correctionId` ASC),
  INDEX `fk_correctionlikes_userId` (`userId` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Table `arcatool`.`correctioncomments`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `arcatool`.`correctioncomments` (
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `correctionId` BIGINT(20) UNSIGNED NOT NULL,
  `userId` BIGINT(20) UNSIGNED NOT NULL,
  `comment` TEXT NOT NULL,
  `updated` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created` TIMESTAMP NULL DEFAULT NULL,
  `tag` SMALLINT(3) DEFAULT '0',
  PRIMARY KEY (`id`),
  INDEX `fk_correctioncomments_1` (`correctionId` ASC),
  INDEX `fk_correctioncomments_2` (`userId` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

CREATE TRIGGER `correctioncomments_created_ts` BEFORE INSERT
ON `arcatool`.`correctioncomments`
FOR EACH ROW
SET NEW.created = CURRENT_TIMESTAMP;

ALTER TABLE `arcatool`.`cause` ADD COLUMN xCoordinate INT DEFAULT '100' NOT NULL;
ALTER TABLE `arcatool`.`cause` ADD COLUMN yCoordinate INT DEFAULT '100' NOT NULL;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
