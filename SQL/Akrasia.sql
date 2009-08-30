CREATE TABLE `dungeonlevel` (
 `session` int(10) unsigned NOT NULL,
 `entry` int(10) unsigned NOT NULL,
 `environment` int(10) NOT NULL,
 PRIMARY KEY (`session`, `entry`)
);

CREATE TABLE creaturetemplate 
( 
  `entry` INT(10) UNSIGNED NOT NULL,
  `name` VARCHAR(255) NULL,
 `char` VARCHAR(9) NOT NULL DEFAULT 'M',
 `levelmin` INT(10) UNSIGNED NOT NULL DEFAULT 1,
 `levelmax` INT(10) UNSIGNED NULL DEFAULT 1,
 `size` INT(10) UNSIGNED NOT NULL DEFAULT 1,
 `type` INT(10) UNSIGNED NOT NULL DEFAULT 1 COMMENT 'Should be an enum',
 `hitdie` INT(10) UNSIGNED NOT NULL DEFAULT 2,
 `armorclass` INT(10) UNSIGNED NOT NULL DEFAULT 0,
 `evade` VARCHAR(255) NOT NULL DEFAULT '0',
 `accuracy` INT(10) UNSIGNED NOT NULL DEFAULT 1,
 `speed` INT(10) UNSIGNED NOT NULL DEFAULT 1,
 `descriptionshort` TEXT NULL,
 `descriptionlong` TEXT NULL,
 `AI` INT(10) NULL,
 CONSTRAINT PRIMARY KEY ( `entry` ) );

CREATE TABLE `creatureinstance` (                                                               
 `session` int(10) unsigned NOT NULL DEFAULT '0',                                              
 `id` int(10) unsigned NOT NULL,                                                               
 `entry` int(10) unsigned DEFAULT NULL,                                                        
 `map` int(10) unsigned DEFAULT NULL,                                                          
 `loc_x` int(10) unsigned DEFAULT NULL,                                                        
 `loc_y` int(10) unsigned DEFAULT NULL,                                                        
 `level` int(10) unsigned DEFAULT NULL,                                                        
 `hp_min` int(10) DEFAULT NULL,                                                                
 `hp_max` int(10) unsigned DEFAULT NULL,                                                       
 PRIMARY KEY (`session`,`id`),                                                                 
 CONSTRAINT `creatureinstance_session` FOREIGN KEY (`session`) REFERENCES `session` (`entry`) 
     ON DELETE NO ACTION,
 CONSTRAINT `creatureinstance_creaturetemplate` FOREIGN KEY (`entry`) REFERENCES `creaturetemplate` (`entry`) 
     ON DELETE NO ACTION,
 CONSTRAINT `creatureinstance_dungeonlevel` FOREIGN KEY ( `session`, `map` ) REFERENCES `dungeonlevel` ( `session`,  `entry` )
     ON DELETE NO ACTION
);