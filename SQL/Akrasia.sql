-- Generate DungeonLevel table
CREATE TABLE IF NOT EXISTS `akrasia`.`DungeonLevel`(
`entry` int(5) UNSIGNED NOT NULL, 
`environment` enum('dungeon', 'city', 'forest', 'plains', 'mountains', 'water') NOT NULL,
PRIMARY KEY (`entry`));