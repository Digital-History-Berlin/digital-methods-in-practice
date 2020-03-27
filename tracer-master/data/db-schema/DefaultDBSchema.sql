-- MySQL dump 10.9
--
-- Host: localhost    Database: MR_schema
-- ------------------------------------------------------
-- Server version	4.1.14-max
-- Based on the latest cvs version, there was added three more
-- tables: co_s_l, co_s_r, sentences_tok
-- $Id: DefaultDBSchema.sql,v 1.5 2009-02-04 07:47:25 steresniak Exp $

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `co_n`
--

DROP TABLE IF EXISTS `co_n`;
CREATE TABLE `co_n` (
  `w1_id` int(10) unsigned NOT NULL default '0',
  `w2_id` int(10) unsigned NOT NULL default '0',
  `freq` int(8) unsigned default NULL,
  `sig` float(8) default NULL,
  KEY `w1_sig` (`w1_id`,`sig`),
  KEY `w2_sig` (`w2_id`,`sig`),
  KEY `w1_w2` (`w2_id`)
) ENGINE=MyISAM;


--
-- Table structure for table `co_s`
--

DROP TABLE IF EXISTS `co_s`;
CREATE TABLE `co_s` (
  `w1_id` int(10) unsigned NOT NULL default '0',
  `w2_id` int(10) unsigned NOT NULL default '0',
  `freq` int(8) unsigned default NULL,
  `sig` float(8) default NULL,
  KEY `w1_sig` (`w1_id`,`sig`),
  KEY `w2_sig` (`w2_id`,`sig`),
  KEY `w1_w2` (`w2_id`)
) ENGINE=MyISAM;


--
-- Table structure for table `co_s_l`
--

DROP TABLE IF EXISTS `co_s_l`;
CREATE TABLE `co_s_l` (
  `w1_id` int(10) unsigned NOT NULL default '0',
  `w2_id` int(10) unsigned NOT NULL default '0',
  `freq` mediumint(8) unsigned default NULL,
  `sig` float(8) default NULL,
  KEY `w1_sig` (`w1_id`,`sig`),
  KEY `w2_sig` (`w2_id`,`sig`),
  KEY `w1_w2` (`w2_id`)
) ENGINE=MyISAM;


--
-- Table structure for table `co_s_r`
--

DROP TABLE IF EXISTS `co_s_r`;
CREATE TABLE `co_s_r` (
  `w1_id` int(10) unsigned NOT NULL default '0',
  `w2_id` int(10) unsigned NOT NULL default '0',
  `freq` mediumint(8) unsigned default NULL,
  `sig` float(8) default NULL,
  KEY `w1_sig` (`w1_id`,`sig`),
  KEY `w2_sig` (`w2_id`,`sig`),
  KEY `w1_w2` (`w2_id`)
) ENGINE=MyISAM;


--
-- Table structure for table `inv_so`
--

DROP TABLE IF EXISTS `inv_so`;
CREATE TABLE `inv_so` (
  `so_id` mediumint(8) unsigned NOT NULL default '0',
  `s_id` int(10) unsigned NOT NULL default '0',
  KEY  `s_id` (`s_id`),
  KEY  `so_id` (`so_id`)
) ENGINE=MyISAM;

--
-- Table structure for table `inv_w`
--

DROP TABLE IF EXISTS `inv_w`;
CREATE TABLE `inv_w` (
  `w_id` int(10) unsigned NOT NULL default '0',
  `s_id` int(10) unsigned NOT NULL default '0',
  KEY `w_id` (`w_id`),
  KEY `s_id` (`s_id`)
) ENGINE=MyISAM;


--
-- Table structure for table `meta`
--

DROP TABLE IF EXISTS `meta`;
CREATE TABLE `meta` (
  `run` mediumint(8) unsigned DEFAULT '0' NOT NULL,
  `attribute` varchar(255) binary DEFAULT '' NOT NULL,
  `value` varchar(255) binary DEFAULT '' NOT NULL,
  KEY `meta` (`run`, `attribute`)
) TYPE=MyISAM DEFAULT CHARSET=utf8 COMMENT='XXX';


--
-- Table structure for table `sentences`
--

DROP TABLE IF EXISTS `sentences`;
CREATE TABLE `sentences` (
  `s_id` int(10) unsigned NOT NULL auto_increment,
  `sentence` text,
  KEY `s_id` (`s_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Table structure for table `sentences_tok`
--

DROP TABLE IF EXISTS `sentences_tok`;
CREATE TABLE `sentences_tok` (
  `s_id` int(10) unsigned NOT NULL auto_increment,
  `sentence` text,
  KEY `s_id` (`s_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Table structure for tables `paras`
--

DROP TABLE IF EXISTS `para_s`;
CREATE TABLE `para_s` (
  `s_id` int(10) unsigned NOT NULL auto_increment,
  `hash` int(10) unsigned,
  `signature_untok` varchar(64),
  `signature_tok` varchar(64),
  `pattern` tinyint(4),
  `lang_name` varchar(3),
  `lang_val` tinyint(4),
    KEY `s_id` (`s_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--
-- Table structure for table `sources`
--

DROP TABLE IF EXISTS `sources`;
CREATE TABLE `sources` (
  `so_id` mediumint(8) unsigned NOT NULL auto_increment,
  `source` varchar(255) character set utf8 collate utf8_bin default NULL,
  `date` date default NULL,
  KEY `so_id` (`so_id`),
  KEY `date` (`date`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--
-- Table structure for table `words`
--

DROP TABLE IF EXISTS `words`;
CREATE TABLE `words` (
  `w_id` int(10) unsigned NOT NULL auto_increment,
  `word` varchar(255) character set utf8 collate utf8_bin default NULL,
  `freq` int(10) unsigned default NULL,
  KEY `w_id` (`w_id`),
  KEY `word` (`word`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

