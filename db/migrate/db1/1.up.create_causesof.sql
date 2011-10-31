-- The user table
CREATE TABLE usercases (
  `cause_id` BIGINT UNSIGNED NOT NULL REFERENCES cause(id),
  `effect_id` BIGINT UNSIGNED NOT NULL REFERENCES cause(id),
  PRIMARY KEY (`cause_id`, `effect_id`)
);
