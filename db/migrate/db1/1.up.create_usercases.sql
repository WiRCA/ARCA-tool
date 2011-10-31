-- The user table
CREATE TABLE usercases (
  `user_id` BIGINT UNSIGNED NOT NULL REFERENCES user(id),
  `case_id` BIGINT UNSIGNED NOT NULL REFERENCES rcacase(id),
  PRIMARY KEY (`userid`, `case_id`)
);
