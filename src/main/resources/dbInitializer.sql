CREATE TABLE IF NOT EXISTS users (
  email VARCHAR PRIMARY KEY,
  user_name VARCHAR NOT NULL,
  password VARCHAR NOT NULL,
  admin INTEGER NOT NULL);

CREATE TABLE IF NOT EXISTS authors (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  author_name VARCHAR NOT NULL);

CREATE TABLE IF NOT EXISTS documents (
  title VARCHAR PRIMARY KEY,
  author_id INTEGER NOT NULL,
  content VARCHAR NOT NULL,
  FOREIGN KEY(author_id) REFERENCES authors(id));