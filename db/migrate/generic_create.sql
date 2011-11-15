SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

CREATE SCHEMA IF NOT EXISTS `${db}` DEFAULT CHARACTER SET utf8 ;

-- A migratable database has to have a 'patchlevel' table, which stores the version and the status of the last update.
CREATE  TABLE IF NOT EXISTS ${db}.`patchlevel` (
  `version` INT(10) UNSIGNED NOT NULL ,
  `status` VARCHAR(255) NULL DEFAULT NULL ,
  PRIMARY KEY (`version`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

insert into ${db}.`patchlevel` (version, status) values (0,'Successful');

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;