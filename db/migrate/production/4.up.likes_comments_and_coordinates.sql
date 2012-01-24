SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';


USE `arcatool` ;

-- -----------------------------------------------------
-- Table `arcatool`.`causelikes`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `arcatool`.`causelikes` (
  `causeId` BIGINT(20) UNSIGNED NOT NULL,
  `userId` BIGINT(20) UNSIGNED NOT NULL,
  PRIMARY KEY (`causeId`, `userId`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Table `arcatool`.`correctionlikes`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `arcatool`.`correctionlikes` (
  `correctionId` BIGINT(20) UNSIGNED NOT NULL,
  `userId` BIGINT(20) UNSIGNED NOT NULL,
  PRIMARY KEY (`correctionId`, `userId`) )
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

ALTER TABLE `arcatool`.`cause` ADD COLUMN xCoordinate INT DEFAULT NULL;
ALTER TABLE `arcatool`.`cause` ADD COLUMN yCoordinate INT DEFAULT NULL;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
