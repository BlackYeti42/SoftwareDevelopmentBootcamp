CREATE DATABASE bank;

CREATE TABLE users(
    id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(16) NOT NULL,
    given_name VARCHAR(70) NOT NULL,
    family_name VARCHAR(600) NOT NULL,
    pin_hash CHAR(161) NOT NULL,
    PRIMARY KEY (id),
) ENGINE=INNODB;

CREATE TABLE addresses(
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    line_1 VARCHAR(35) NOT NULL,
    line_2 VARCHAR(35),
    line_3 VARCHAR(35),
    line_4 VARCHAR(35),
    city VARCHAR(85) NOT NULL,
    county VARCHAR(35),
    postcode VARCHAR(8) NOT NULL,
    country_code CHAR(2) NOT NULL,
    email VARCHAR(30) NOT NULL,
    telephone_1 VARCHAR(15) NOT NULL,
    telephone_2 VARCHAR(15),
    PRIMARY KEY(id),
    FOREIGN KEY(user_id) REFERENCES users(id)
) ENGINE=INNODB;

CREATE TABLE accounts(
    id INT NOT NULL AUTO_INCREMENT,
    account_number INT NOT NULL,
    balance DECIMAL(13,2) NOT NULL,
    account_type INT NOT NULL,
    restriction BIT NOT NULL,
    PRIMARY KEY(id),
    UNIQUE(account_number)
) ENGINE=INNODB;

CREATE TABLE users_accounts(
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    account_id INT,
    PRIMARY KEY(id),
    FOREIGN KEY(user_id) REFERENCES users(id),
    FOREIGN KEY(account_id) REFERENCES accounts(id)
) ENGINE=INNODB;

CREATE TABLE transfer_history(
     id INT NOT NULL AUTO_INCREMENT,
     source_account_id INT,
     destination_account_id INT,
     transfer_time DATETIME NOT NULL,
     ammount DECIMAL(13,2) NOT NULL,
     PRIMARY KEY(id),
     FOREIGN KEY(source_account_id) REFERENCES accounts(id),
     FOREIGN KEY(destination_account_id) REFERENCES accounts(id),
     CHECK ((source_account_id IS NULL AND destination_account_id IS NULL) OR (source_account_id IS NOT NULL AND destination_account_id IS NOT NULL))
) ENGINE=INNODB;

CREATE TABLE deposit_history(
    id INT NOT NULL AUTO_INCREMENT,
    account_id INT,
    deposit_time DATETIME NOT NULL,
    ammount DECIMAL(13,2) NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY(account_id) REFERENCES accounts(id)
) ENGINE=INNODB;

CREATE TABLE withdrawal_history(
    id INT NOT NULL AUTO_INCREMENT,
    account_id INT,
    withdrawal_time DATETIME NOT NULL,
    ammount DECIMAL(13,2) NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY(account_id) REFERENCES accounts(id)
) ENGINE=INNODB;