# Tasks schema
 
# --- !Ups

CREATE TABLE abilities(
  id   LONG         UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(255)          NOT NULL,

  PRIMARY KEY (id)
);

CREATE TABLE natures(
  id   LONG         UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(255)          NOT NULL,

  PRIMARY KEY (id)
);

CREATE TABLE ev_spreads(
  id      LONG    UNSIGNED NOT NULL AUTO_INCREMENT,
  hp      INT     UNSIGNED NOT NULL,
  attack  INT     UNSIGNED NOT NULL,
  defence INT     UNSIGNED NOT NULL,
  spa     INT     UNSIGNED NOT NULL,
  spd     INT     UNSIGNED NOT NULL,
  speed   INT     UNSIGNED NOT NULL,

  PRIMARY KEY (id)
);

CREATE TABLE moves(
  id   LONG         UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(255)          NOT NULL,

  PRIMARY KEY (id)
);
CREATE TABLE items(
  id   LONG         UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(255)          NOT NULL,

  PRIMARY KEY (id)
);

CREATE TABLE pokemon(
  id   LONG         UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(255)          NOT NULL,

  PRIMARY KEY (id)
);

CREATE TABLE generations(
  id     LONG    UNSIGNED NOT NULL AUTO_INCREMENT,
  number INT     UNSIGNED NOT NULL UNIQUE,

  PRIMARY KEY (id)
);

CREATE TABLE tiers(
  id            LONG         UNSIGNED NOT NULL AUTO_INCREMENT,
  name          VARCHAR(255)          NOT NULL,

  generation_id INT          UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (generation_id) REFERENCES generations(id),

  CONSTRAINT one_named_tier_per_generation UNIQUE (name, generation_id)
);

CREATE TABLE years(
  id     LONG     UNSIGNED NOT NULL AUTO_INCREMENT,
  number INT      UNSIGNED NOT NULL,

  PRIMARY KEY (id)
);

CREATE TABLE months(
  id      LONG         UNSIGNED NOT NULL AUTO_INCREMENT,
  number  INT          UNSIGNED NOT NULL,

  year_id INT          UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (year_id) REFERENCES years(id)
);

CREATE TABLE tier_months(
  id       LONG UNSIGNED NOT NULL AUTO_INCREMENT,

  month_id INT  UNSIGNED NOT NULL,
  tier_id  INT  UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (month_id) REFERENCES months(id),
  FOREIGN KEY (tier_id)  REFERENCES tiers(id)
);

CREATE TABLE tier_ratings(
  id            LONG UNSIGNED NOT NULL AUTO_INCREMENT,
  no_of_battles INT  UNSIGNED NOT NULL,

  rating        INT  UNSIGNED NOT NULL,
  tier_month_id INT  UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (tier_month_id) REFERENCES tier_months(id)
);

CREATE TABLE stat_records(
  id             LONG UNSIGNED NOT NULL AUTO_INCREMENT,
  raw_usage      INT  UNSIGNED NOT NULL,

  pokemon_id     INT  UNSIGNED NOT NULL,
  tier_rating_id INT  UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (pokemon_id)     REFERENCES pokemon(id),
  FOREIGN KEY (tier_rating_id) REFERENCES tier_ratings(id)
);

CREATE TABLE ability_records(
  id             LONG   UNSIGNED NOT NULL AUTO_INCREMENT,
  number         FLOAT           NOT NULL,

  ability_id     INT    UNSIGNED NOT NULL,
  stat_record_id INT    UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (ability_id)     REFERENCES abilities(id),
  FOREIGN KEY (stat_record_id) REFERENCES stat_records(id)
);

CREATE TABLE check_records(
  id             LONG  UNSIGNED NOT NULL AUTO_INCREMENT,
  number         FLOAT          NOT NULL,

  pokemon_id     INT   UNSIGNED NOT NULL,
  stat_record_id INT   UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (pokemon_id)     REFERENCES pokemon(id),
  FOREIGN KEY (stat_record_id) REFERENCES stat_records(id)
);

CREATE TABLE item_records(
  id             LONG  UNSIGNED NOT NULL AUTO_INCREMENT,
  number         FLOAT          NOT NULL,

  item_id        INT   UNSIGNED NOT NULL,
  stat_record_id INT   UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (item_id)        REFERENCES items(id),
  FOREIGN KEY (stat_record_id) REFERENCES stat_records(id)
);

CREATE TABLE lead_records(
  id             LONG  UNSIGNED NOT NULL AUTO_INCREMENT,
  number         FLOAT          NOT NULL,

  stat_record_id INT   UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (stat_record_id) REFERENCES stat_records(id)
);

CREATE TABLE move_records(
  id             LONG  UNSIGNED NOT NULL AUTO_INCREMENT,
  number         FLOAT          NOT NULL,

  move_id        INT   UNSIGNED NOT NULL,
  stat_record_id INT   UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (move_id)    REFERENCES moves(id),
  FOREIGN KEY (stat_record_id) REFERENCES stat_records(id)
);

CREATE TABLE spread_records(
  id             LONG  UNSIGNED NOT NULL AUTO_INCREMENT,
  number         FLOAT        NOT NULL,

  ev_spread_id   INT   UNSIGNED NOT NULL,
  nature_id      INT   UNSIGNED NOT NULL,
  stat_record_id INT   UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (ev_spread_id)   REFERENCES ev_spreads(id),
  FOREIGN KEY (nature_id)      REFERENCES natures(id),
  FOREIGN KEY (stat_record_id) REFERENCES stat_records(id)
);

CREATE TABLE teammate_records(
  id     LONG        UNSIGNED NOT NULL AUTO_INCREMENT,
  number FLOAT                NOT NULL,

  pokemon_id     INT UNSIGNED NOT NULL,
  stat_record_id INT UNSIGNED NOT NULL,

  PRIMARY KEY (id),

  FOREIGN KEY (pokemon_id)     REFERENCES pokemon(id),
  FOREIGN KEY (stat_record_id) REFERENCES stat_records(id)
);