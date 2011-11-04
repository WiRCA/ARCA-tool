CREATE TABLE cause (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(255),
  creator_id BIGINT UNSIGNED NOT NULL,
  case_id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (case_id) REFERENCES rcacase(id) ON DELETE CASCADE,
  FOREIGN KEY (creator_id) REFERENCES arcatooluser.user(id) ON DELETE SET NULL
);

-- The corrections table
CREATE TABLE corrections (
  cause_id BIGINT UNSIGNED NOT NULL,
  correction VARCHAR(255),
  PRIMARY KEY (cause_id, correction),
  FOREIGN KEY (cause_id) REFERENCES cause(id)
);

CREATE TABLE rcacase (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(255),
  problem_id BIGINT UNSIGNED NOT NULL,
  owner_id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (problem_id) REFERENCES cause(id) ON DELETE SET NULL,
  FOREIGN KEY (owner_id) REFERENCES arcatooluser.user(id) ON DELETE SET NULL
);

CREATE TABLE usercases (
  user_id BIGINT UNSIGNED NOT NULL,
  case_id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (user_id, case_id),
  FOREIGN KEY (case_id) REFERENCES rcacase(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES arcatooluser.user(id) ON DELETE CASCADE
);

CREATE TABLE causesof (
  cause_id BIGINT UNSIGNED NOT NULL,
  effect_id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (cause_id, effect_id),
  FOREIGN KEY (cause_id) REFERENCES cause(id) ON DELETE CASCADE,
  FOREIGN KEY (effect_id) REFERENCES cause(id) ON DELETE CASCADE
);

-- The user table
CREATE TABLE arcatooluser.user (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  email VARCHAR(255) NOT NULL UNIQUE,
  name VARCHAR(255),
  password VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);