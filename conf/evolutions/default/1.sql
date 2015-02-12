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
  hp      SMALLINT UNSIGNED NOT NULL,
  attack  SMALLINT UNSIGNED NOT NULL,
  defence SMALLINT UNSIGNED NOT NULL,
  spa     SMALLINT UNSIGNED NOT NULL,
  spd     SMALLINT UNSIGNED NOT NULL,
  speed   SMALLINT UNSIGNED NOT NULL,

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
  number  SMALLINT     UNSIGNED NOT NULL,

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

CREATE TABLE tier_ratings(
  id            INT UNSIGNED NOT NULL AUTO_INCREMENT,
  no_of_battles INT UNSIGNED NOT NULL,

  rating        INT UNSIGNED NOT NULL,
  tier_month_id INT UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (tier_month_id) REFERENCES tier_ratings(id)
);

CREATE TABLE stat_records(
  id             INT UNSIGNED NOT NULL AUTO_INCREMENT,
  raw_usage      INT UNSIGNED NOT NULL,

  pokemon_id     INT UNSIGNED NOT NULL,
  tier_rating_id INT UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (pokemon_id)     REFERENCES pokemon(id),
  FOREIGN KEY (tier_rating_id) REFERENCES tier_ratings(id)
);

CREATE TABLE ability_records(
  id             INT    UNSIGNED NOT NULL AUTO_INCREMENT,
  number         FLOAT           NOT NULL,

  ability_id     INT    UNSIGNED NOT NULL,
  stat_record_id INT    UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (ability_id)     REFERENCES abilities(id),
  FOREIGN KEY (stat_record_id) REFERENCES stat_records(id)
);

CREATE TABLE check_records(
  id             INT   UNSIGNED NOT NULL AUTO_INCREMENT,
  number         FLOAT          NOT NULL,

  pokemon_id     INT   UNSIGNED NOT NULL,
  stat_record_id INT   UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (pokemon_id)     REFERENCES pokemon(id),
  FOREIGN KEY (stat_record_id) REFERENCES stat_records(id)
);

CREATE TABLE item_records(
  id             INT   UNSIGNED NOT NULL AUTO_INCREMENT,
  number         FLOAT          NOT NULL,

  item_id        INT   UNSIGNED NOT NULL,
  stat_record_id INT   UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (item_id)        REFERENCES items(id),
  FOREIGN KEY (stat_record_id) REFERENCES stat_records(id)
);

CREATE TABLE lead_records(
  id             INT   UNSIGNED NOT NULL AUTO_INCREMENT,
  number         FLOAT          NOT NULL,

  stat_record_id INT   UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (stat_record_id) REFERENCES stat_records(id)
);

CREATE TABLE move_records(
  id             INT   UNSIGNED NOT NULL AUTO_INCREMENT,
  number         FLOAT          NOT NULL,

  move_id        INT   UNSIGNED NOT NULL,
  stat_record_id INT   UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (move_id)    REFERENCES moves(id),
  FOREIGN KEY (stat_record_id) REFERENCES stat_records(id)
);

CREATE TABLE spread_records(
  id             INT UNSIGNED NOT NULL AUTO_INCREMENT,
  number         FLOAT        NOT NULL,

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
  number FLOAT        NOT NULL,

  pokemon_id     INT UNSIGNED NOT NULL,
  stat_record_id INT UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (pokemon_id)     REFERENCES pokemon(id),
  FOREIGN KEY (stat_record_id) REFERENCES stat_records(id)
);