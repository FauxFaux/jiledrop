--liquibase formatted sql

--changeset jd:1

CREATE TABLE users (
  username VARCHAR(255) PRIMARY KEY,
  password VARCHAR(255) NOT NULL,
  target   VARCHAR(36)  NOT NULL
);
