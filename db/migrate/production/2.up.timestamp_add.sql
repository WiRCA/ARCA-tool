ALTER TABLE `arcatool`.`rcacase` ADD COLUMN created DATETIME NOT NULL;
ALTER TABLE `arcatool`.`rcacase` ADD COLUMN updated DATETIME NOT NULL;

ALTER TABLE `arcatool`.`cause` ADD COLUMN updated DATETIME NOT NULL;