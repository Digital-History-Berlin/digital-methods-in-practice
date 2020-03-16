--
-- Default database schema --
--

--
-- Table structure for table `ruid2feat`
--

DROP TABLE IF EXISTS `ruid2feat`;
CREATE TABLE `ruid2feat` (
  `f_id` int(10) unsigned NOT NULL default '0',
  `ruid` int(10) unsigned NOT NULL default '0',
  KEY `ruid` (`ruid`),
  KEY `f_id` (`f_id`)
) ENGINE=MyISAM CHARSET=utf8;

--
-- Table structure for table `sentences`
--

DROP TABLE IF EXISTS `reuse_units`;
CREATE TABLE `reuse_units` (
  `ruid` int(10) unsigned NOT NULL auto_increment,
  `reuse_unit` text,
  PRIMARY KEY `ruid` (`ruid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;


--
-- Table structure for table `features`
--

DROP TABLE IF EXISTS `features`;
CREATE TABLE `features` (
  `f_id` int(10) unsigned NOT NULL auto_increment,
  `feature` varchar(255) character set utf8 collate utf8_bin default NULL,
  PRIMARY KEY `f_id` (`f_id`),
  KEY `feature` (`feature`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

