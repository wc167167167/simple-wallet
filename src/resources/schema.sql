DROP TABLE IF EXISTS wallet;
CREATE TABLE wallet(
    version BIGINT PRIMARY KEY,
    ts_millis BIGINT,
    content TEXT,
    total BIGINT
)