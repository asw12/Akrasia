CREATE TABLE dungeonlevel 
( 
  `entry` INT(10) UNSIGNED NOT NULL 
, `environment` INT(10) NOT NULL 
, CONSTRAINT PRIMARY KEY ( `entry` ) );

CREATE TABLE creaturetemplate 
( 
  `entry` INT(10) UNSIGNED NOT NULL 
, `name` VARCHAR(255) NULL 
, `char` VARCHAR(9) NOT NULL DEFAULT 'M' 
, `levelmin` INT(10) UNSIGNED NOT NULL DEFAULT 1 
, `levelmax` INT(10) UNSIGNED NULL DEFAULT 1 
, `size` INT(10) UNSIGNED NOT NULL DEFAULT 1 
, `type` INT(10) UNSIGNED NOT NULL DEFAULT 1 COMMENT 'Should be an enum' 
, `hitdie` INT(10) UNSIGNED NOT NULL DEFAULT 2 
, `armorclass` INT(10) UNSIGNED NOT NULL DEFAULT 0 
, `evade` VARCHAR(255) NOT NULL DEFAULT '0' 
, `accuracy` INT(10) UNSIGNED NOT NULL DEFAULT 1 
, `speed` INT(10) UNSIGNED NOT NULL DEFAULT 1 
, `descriptionshort` TEXT NULL 
, `descriptionlong` TEXT NULL 
, `AI` INT(10) NULL 
, CONSTRAINT PRIMARY KEY ( `entry` ) );
