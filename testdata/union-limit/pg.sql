CREATE TABLE union_limit_root (
  id VARCHAR PRIMARY KEY,
  name VARCHAR
);

CREATE TABLE union_limit_lista (
  id VARCHAR PRIMARY KEY,
  root_id VARCHAR
);

CREATE TABLE union_limit_listb (
  id VARCHAR PRIMARY KEY,
  root_id VARCHAR
);

CREATE TABLE union_limit_assoc (
  root_id VARCHAR,
  target_id VARCHAR,
  PRIMARY KEY (root_id, target_id)
);

CREATE TABLE union_limit_target (
  id VARCHAR PRIMARY KEY,
  name VARCHAR
);
