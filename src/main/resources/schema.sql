CREATE TABLE if not exists objects (
  id BIGSERIAL PRIMARY KEY,
  type VARCHAR(30),
  source_id VARCHAR(500),
  source_path VARCHAR(500),
  target_id VARCHAR(500),
  target_path VARCHAR(500),
  status VARCHAR(10),
  config_id UUID
);

create index if not exists idx_config_id ON objects (config_id);
