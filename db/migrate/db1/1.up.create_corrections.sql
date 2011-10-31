-- The user table
CREATE TABLE usercases (
  `cause_id` BIGINT UNSIGNED NOT NULL REFERENCES cause(id),
  `correction` VARCHAR(255),
  PRIMARY KEY (`cause_id`, `correction`)
);
