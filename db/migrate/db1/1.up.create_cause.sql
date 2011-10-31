-- The user table
CREATE TABLE cause (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255),
  `creator_id` BIGINT UNSIGNED NOT NULL REFERENCES user(id),
  `case_id` BIGINT UNSIGNED NOT NULL REFERENCES rcacase(id),
  PRIMARY KEY (`id`)
);
