DROP TABLE usercases;

CREATE TABLE wircatestuser.usercases (
  user_id BIGINT UNSIGNED NOT NULL,
  case_id BIGINT,
  PRIMARY KEY (user_id, case_id),
  FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

ALTER TABLE cause DROP COLUMN case_id;

ALTER TABLE rcacase ADD COLUMN company_name VARCHAR(255);
ALTER TABLE rcacase ADD COLUMN case_type_value SMALLINT;
ALTER TABLE rcacase ADD COLUMN company_size_value SMALLINT;
ALTER TABLE rcacase ADD COLUMN is_multinational TINYINT(1) DEFAULT 0;
ALTER TABLE rcacase ADD COLUMN is_case_public TINYINT(1) DEFAULT 0;