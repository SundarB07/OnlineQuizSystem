CREATE DATABASE quizdb;
USE quizdb;

CREATE TABLE users (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100),
  role ENUM('student','teacher'),
  username VARCHAR(50) UNIQUE,
  password VARCHAR(50)
);

CREATE TABLE quiz (
  quiz_id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(100),
  duration INT,
  created_by INT,
  FOREIGN KEY (created_by) REFERENCES users(user_id)
);

CREATE TABLE questions (
  q_id INT AUTO_INCREMENT PRIMARY KEY,
  quiz_id INT,
  question_text VARCHAR(255),
  optionA VARCHAR(100),
  optionB VARCHAR(100),
  optionC VARCHAR(100),
  optionD VARCHAR(100),
  correct_option VARCHAR(1),
  FOREIGN KEY (quiz_id) REFERENCES quiz(quiz_id)
);

CREATE TABLE results (
  result_id INT AUTO_INCREMENT PRIMARY KEY,
  student_id INT,
  quiz_id INT,
  score INT,
  date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (student_id) REFERENCES users(user_id),
  FOREIGN KEY (quiz_id) REFERENCES quiz(quiz_id)
);
