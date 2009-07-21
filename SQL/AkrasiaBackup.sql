/*
SQLyog Community Edition- MySQL GUI v7.15 
MySQL - 5.1.30-community : Database - akrasia
*********************************************************************
*/


/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

/*Table structure for table `creaturetemplate` */

CREATE TABLE IF NOT EXISTS `creaturetemplate` (
  `entry` int(10) unsigned NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `char` varchar(9) NOT NULL DEFAULT 'M',
  `levelmin` int(10) unsigned NOT NULL DEFAULT '1',
  `levelmax` int(10) unsigned DEFAULT '1',
  `size` int(10) unsigned NOT NULL DEFAULT '1',
  `type` int(10) unsigned NOT NULL DEFAULT '1' COMMENT 'Should be an enum',
  `hitdie` int(10) unsigned NOT NULL DEFAULT '2',
  `armorclass` int(10) unsigned NOT NULL DEFAULT '0',
  `evade` varchar(255) NOT NULL DEFAULT '0',
  `accuracy` int(10) unsigned NOT NULL DEFAULT '1',
  `speed` int(10) unsigned NOT NULL DEFAULT '1',
  `descriptionshort` text,
  `descriptionlong` text,
  `AI` int(11) DEFAULT NULL,
  PRIMARY KEY (`entry`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `dungeonlevel` */

CREATE TABLE IF NOT EXISTS `dungeonlevel` (
  `entry` int(5) unsigned NOT NULL,
  `environment` enum('dungeon','city','forest','plains','mountains','water') NOT NULL,
  PRIMARY KEY (`entry`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;