-- The user table
CREATE TABLE user (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(255),
  `name` VARCHAR(255),
  `password` VARCHAR(255),
  PRIMARY KEY (`id`)
);
