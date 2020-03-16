-- MySQL dump 10.9
--
-- Host: localhost    Database: MR_schema
-- ------------------------------------------------------
-- Server version	4.1.14-max
-- Based on the latest cvs version, there was added three more
-- tables: co_s_l, co_s_r, sentences_tok


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

DROP TABLE `words` IF EXISTS
CREATE TABLE `words` (
  `w_id` int(8) unsigned NOT NULL default '0',
  `word` varchar(255) binary,
  `freq` int(8) unsigned default NULL,
  PRIMARY KEY  (`w_id`),
  KEY `word` (`word`)
);

DROP TABLE `word_date` IF EXISTS
CREATE TABLE `word_date` (
  `w_id` mediumint(8) unsigned NOT NULL default '0',
  `date` date NOT NULL default '0000-00-00',
  `freq` mediumint(9) NOT NULL default '0',
  KEY `date` (`date`),
  KEY `w_id` (`w_id`)
);

DROP TABLE `co_s_date` IF EXISTS
CREATE TABLE `co_s_date` (
  `w1_id` mediumint(8) unsigned NOT NULL default '0',
  `w2_id` mediumint(8) unsigned NOT NULL default '0',
  `date` date NOT NULL default '0000-00-00',
  `freq` int(10) unsigned NOT NULL default '0',
  `sig` mediumint(8) unsigned NOT NULL default '0',
  KEY `w1_id` (`w1_id`),
  KEY `date` (`date`)
);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

