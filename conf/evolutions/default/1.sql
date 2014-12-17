# Tasks schema
 
# --- !Ups

CREATE TABLE abilities(
  id   INT          UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(255)          NOT NULL,

  PRIMARY KEY (id)
);

CREATE TABLE natures(
  id   INT          UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(255)          NOT NULL,

  PRIMARY KEY (id)
);

CREATE TABLE ev_spreads(
  id      INT     UNSIGNED NOT NULL AUTO_INCREMENT,
  hp      TINYINT UNSIGNED NOT NULL,
  attack  TINYINT UNSIGNED NOT NULL,
  defence TINYINT UNSIGNED NOT NULL,
  spa     TINYINT UNSIGNED NOT NULL,
  spd     TINYINT UNSIGNED NOT NULL,
  speed   TINYINT UNSIGNED NOT NULL,

  PRIMARY KEY (id)
);

CREATE TABLE moves(
  id   INT          UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(255)          NOT NULL,

  PRIMARY KEY (id)
);
CREATE TABLE items(
  id   INT          UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(255)          NOT NULL,

  PRIMARY KEY (id)
);

CREATE TABLE pokemon(
  id   INT          UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(255)          NOT NULL,

  PRIMARY KEY (id)
);

CREATE TABLE generations(
  id     INT     UNSIGNED NOT NULL AUTO_INCREMENT,
  number TINYINT UNSIGNED NOT NULL UNIQUE,

  PRIMARY KEY (id)
);

CREATE TABLE tiers(
  id            INT          UNSIGNED NOT NULL AUTO_INCREMENT,
  name          VARCHAR(255)          NOT NULL,

  generation_id INT          UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (generation_id) REFERENCES generations(id),

  CONSTRAINT one_named_tier_per_generation UNIQUE (name, generation_id)
);

CREATE TABLE years(
  id     INT      UNSIGNED NOT NULL AUTO_INCREMENT,
  number SMALLINT UNSIGNED NOT NULL,

  PRIMARY KEY (id)
);

CREATE TABLE months(
  id      INT          UNSIGNED NOT NULL AUTO_INCREMENT,
  name    VARCHAR(255)          NOT NULL,

  year_id INT          UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (year_id) REFERENCES years(id)
);

CREATE TABLE tier_months(
  id       INT UNSIGNED NOT NULL AUTO_INCREMENT,

  month_id INT UNSIGNED NOT NULL,
  tier_id  INT UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (month_id) REFERENCES months(id),
  FOREIGN KEY (tier_id)  REFERENCES tiers(id)
);

CREATE TABLE stat_records(
  id            INT UNSIGNED NOT NULL AUTO_INCREMENT,

  pokemon_id    INT UNSIGNED NOT NULL,
  tier_month_id INT UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (pokemon_id)    REFERENCES pokemon(id),
  FOREIGN KEY (tier_month_id) REFERENCES tier_months(id)
);

CREATE TABLE ability_records(
  id             INT UNSIGNED NOT NULL AUTO_INCREMENT,
  number         INT UNSIGNED NOT NULL,

  ability_id     INT UNSIGNED NOT NULL,
  stat_record_id INT UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (ability_id)     REFERENCES abilities(id),
  FOREIGN KEY (stat_record_id) REFERENCES stat_records(id)
);

CREATE TABLE check_records(
  id             INT UNSIGNED NOT NULL AUTO_INCREMENT,
  number         INT UNSIGNED NOT NULL,

  pokemon_id     INT UNSIGNED NOT NULL,
  stat_record_id INT UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (pokemon_id)     REFERENCES pokemon(id),
  FOREIGN KEY (stat_record_id) REFERENCES stat_records(id)
);

CREATE TABLE item_records(
  id             INT UNSIGNED NOT NULL AUTO_INCREMENT,
  number         INT UNSIGNED NOT NULL,

  item_id        INT UNSIGNED NOT NULL,
  stat_record_id INT UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (item_id)        REFERENCES items(id),
  FOREIGN KEY (stat_record_id) REFERENCES stat_records(id)
);

CREATE TABLE lead_records(
  id             INT UNSIGNED NOT NULL AUTO_INCREMENT,
  number         INT UNSIGNED NOT NULL,

  stat_record_id INT UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (stat_record_id) REFERENCES stat_records(id)
);

CREATE TABLE moveset_records(
  id             INT UNSIGNED NOT NULL AUTO_INCREMENT,
  number         INT UNSIGNED NOT NULL,

  move_one_id    INT UNSIGNED NOT NULL,
  move_two_id    INT UNSIGNED NOT NULL,
  move_three_id  INT UNSIGNED NOT NULL,
  move_four_id   INT UNSIGNED NOT NULL,
  stat_record_id INT UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (move_one_id)    REFERENCES moves(id),
  FOREIGN KEY (move_two_id)    REFERENCES moves(id),
  FOREIGN KEY (move_three_id)  REFERENCES moves(id),
  FOREIGN KEY (move_four_id)   REFERENCES moves(id),
  FOREIGN KEY (stat_record_id) REFERENCES stat_records(id)
);

CREATE TABLE spread_records(
  id             INT UNSIGNED NOT NULL AUTO_INCREMENT,
  number         INT UNSIGNED NOT NULL,

  ev_spread_id   INT UNSIGNED NOT NULL,
  nature_id      INT UNSIGNED NOT NULL,
  stat_record_id INT UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (ev_spread_id)   REFERENCES ev_spreads(id),
  FOREIGN KEY (nature_id)      REFERENCES natures(id),
  FOREIGN KEY (stat_record_id) REFERENCES stat_records(id)
);

CREATE TABLE teammate_records(
  id     INT UNSIGNED NOT NULL AUTO_INCREMENT,
  number INT UNSIGNED NOT NULL,

  pokemon_id     INT UNSIGNED NOT NULL,
  stat_record_id INT UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (pokemon_id)     REFERENCES pokemon(id),
  FOREIGN KEY (stat_record_id) REFERENCES stat_records(id)
);