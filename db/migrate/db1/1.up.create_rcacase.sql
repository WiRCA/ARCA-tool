-- The user table
CREATE TABLE rcacase (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255),
  `problem_id` BIGINT UNSIGNED NOT NULL REFERENCES cause(id),
  `owner_id` BIGINT UNSIGNED NOT NULL REFERENCES user(id),
  PRIMARY KEY (`id`)
);
