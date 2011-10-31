-- The causes table
CREATE TABLE cause (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255),
  `creator_id` BIGINT UNSIGNED NOT NULL,
  `case_id` BIGINT UNSIGNED NOT NULL REFERENCES rcacase(id),
  PRIMARY KEY (`id`)
);

-- The corrections table
CREATE TABLE corrections (
  `cause_id` BIGINT UNSIGNED NOT NULL REFERENCES cause(id),
  `correction` VARCHAR(255),
  PRIMARY KEY (`cause_id`, `correction`)
);

CREATE TABLE rcacase (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255),
  `problem_id` BIGINT UNSIGNED NOT NULL REFERENCES cause(id),
  `owner_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE usercases (
  `user_id` BIGINT UNSIGNED NOT NULL,
  `case_id` BIGINT UNSIGNED NOT NULL REFERENCES rcacase(id),
  PRIMARY KEY (`userid`, `case_id`)
);

CREATE TABLE causesof (
  `cause_id` BIGINT UNSIGNED NOT NULL REFERENCES cause(id),
  `effect_id` BIGINT UNSIGNED NOT NULL REFERENCES cause(id),
  PRIMARY KEY (`cause_id`, `effect_id`)
);

