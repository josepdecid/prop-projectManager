CREATE TABLE IF NOT EXISTS users (
  email VARCHAR PRIMARY KEY,                                -- User's email
  user_name VARCHAR NOT NULL,                               -- User's name
  password VARCHAR NOT NULL,                                -- User's hashed password
  admin INTEGER NOT NULL);                                  -- Admin -> 1 | StdUser -> 0

CREATE TABLE IF NOT EXISTS authors (
  author_name VARCHAR PRIMARY KEY);                         -- Author name

CREATE TABLE IF NOT EXISTS documents (
  title VARCHAR,                                            -- Document title
  author_name VARCHAR NOT NULL,                             -- Author name
  user_owner VARCHAR NOT NULL,                              -- User that creates the file
  term_frequency VARCHAR NOT NULL,                          -- JSON with tf Data
  content VARCHAR NOT NULL,                                 -- Content location in /documents
  PRIMARY KEY(title, author_name),
  FOREIGN KEY(author_name) REFERENCES authors(author_name),
  FOREIGN KEY(user_owner) REFERENCES users(email));