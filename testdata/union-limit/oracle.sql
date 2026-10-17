CREATE TABLE union_limit_root (
  id VARCHAR(100) PRIMARY KEY,
  name VARCHAR(100)
);

CREATE TABLE union_limit_lista (
  id VARCHAR(100) PRIMARY KEY,
  root_id VARCHAR(100)
);

CREATE TABLE union_limit_listb (
  id VARCHAR(100) PRIMARY KEY,
  root_id VARCHAR(100)
);

CREATE TABLE union_limit_assoc (
  root_id VARCHAR(100),
  target_id VARCHAR(100),
  PRIMARY KEY (root_id, target_id)
);

CREATE TABLE union_limit_target (
  id VARCHAR(100) PRIMARY KEY,
  name VARCHAR(100)
);
