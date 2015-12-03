use optima;
-- MySQL dump 10.13  Distrib 5.6.24, for Win32 (x86)
--
-- Host: localhost    Database: optima
-- ------------------------------------------------------
-- Server version	5.6.24

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `client`
-- 

DROP TABLE IF EXISTS `client`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `client` (
  `client_id` int(11) NOT NULL AUTO_INCREMENT,
  `client_name` varchar(100) NOT NULL,
  `client_address_street` varchar(100) DEFAULT NULL,
  `client_address_city` int(11) DEFAULT NULL,
  `client_address_province` int(11) DEFAULT NULL,
  `client_address_country` int(11) DEFAULT NULL,
  `client_address_postal_code` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`client_id`),
  KEY `client_name_idx` (`client_name`),
  KEY `client_address_city` (`client_address_city`),
  KEY `client_address_province` (`client_address_province`),
  KEY `client_address_country` (`client_address_country`),
  CONSTRAINT `client_ibfk_1` FOREIGN KEY (`client_address_city`) REFERENCES `location_info` (`location_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `client_ibfk_2` FOREIGN KEY (`client_address_province`) REFERENCES `location_info` (`location_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `client_ibfk_3` FOREIGN KEY (`client_address_country`) REFERENCES `location_info` (`location_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `client`
--

LOCK TABLES `client` WRITE;
/*!40000 ALTER TABLE `client` DISABLE KEYS */;
INSERT INTO `client` VALUES (1,'Client 1','15 Bank St',3,2,1,'K1K9U8'),(2,'client2','17 gray crescent',5,2,1,'K2k 3j4'),(3,'client 3','17 gray cres.',5,4,1,'n4s n45');
/*!40000 ALTER TABLE `client` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `days_off`
--

DROP TABLE IF EXISTS `days_off`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `days_off` (
  `dayoff_id` int(11) NOT NULL AUTO_INCREMENT,
  `project_id` int(11) DEFAULT NULL,
  `day_off` date DEFAULT NULL,
  `dayoff_type` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`dayoff_id`),
  UNIQUE KEY `project_id` (`project_id`,`day_off`),
  CONSTRAINT `days_off_ibfk_1` FOREIGN KEY (`project_id`) REFERENCES `project` (`project_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `days_off`
--

LOCK TABLES `days_off` WRITE;
/*!40000 ALTER TABLE `days_off` DISABLE KEYS */;
/*!40000 ALTER TABLE `days_off` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `location_info`
--

DROP TABLE IF EXISTS `location_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `location_info` (
  `location_id` int(11) NOT NULL AUTO_INCREMENT,
  `location_name` varchar(50) NOT NULL,
  `location_type` varchar(10) DEFAULT NULL,
  `parent_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`location_id`),
  KEY `location_paret_id` (`parent_id`),
  KEY `location_name_idx` (`location_name`),
  CONSTRAINT `location_paret_id` FOREIGN KEY (`parent_id`) REFERENCES `location_info` (`location_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `location_info`
--

LOCK TABLES `location_info` WRITE;
/*!40000 ALTER TABLE `location_info` DISABLE KEYS */;
INSERT INTO `location_info` VALUES (1,'Canada','COUNTRY',NULL),(2,'Ontario','PROVINCE',1),(3,'Ottawa','CITY',2),(4,'Quebec','PROVINCE',1),(5,'Montreal','CITY',4);
/*!40000 ALTER TABLE `location_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment_type`
--

DROP TABLE IF EXISTS `payment_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `payment_type` (
  `payment_type_id` int(11) NOT NULL AUTO_INCREMENT,
  `payment_type` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`payment_type_id`),
  UNIQUE KEY `payment_type` (`payment_type`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_type`
--

LOCK TABLES `payment_type` WRITE;
/*!40000 ALTER TABLE `payment_type` DISABLE KEYS */;
INSERT INTO `payment_type` VALUES (1,'Advance'),(2,'Intrim');
/*!40000 ALTER TABLE `payment_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `portfolio`
--

DROP TABLE IF EXISTS `portfolio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `portfolio` (
  `portfolio_id` int(11) NOT NULL AUTO_INCREMENT,
  `portfolio_name` varchar(32) DEFAULT NULL,
  `portfolio_descreption` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`portfolio_id`),
  KEY `portofolio_name_idx` (`portfolio_name`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

ALTER TABLE portfolio
ADD CONSTRAINT uc_portfolio_name UNIQUE (portfolio_name);
--
-- Dumping data for table `portfolio`
--

LOCK TABLES `portfolio` WRITE;
/*!40000 ALTER TABLE `portfolio` DISABLE KEYS */;
INSERT INTO `portfolio` VALUES (1,'Default Portfolio','Default Portfolio. All projects in the same portfolio are financed together.'),(4,'Two projects','Two projects'),(5,'27ActivitiesProj','27ActivitiesProj'),(6,'2-25 Activities_Green','2-25 Tasks'),(7,'2-25 Activities_New','2-25 Activities_New'),(8,'One project (green)','One project (green)');
/*!40000 ALTER TABLE `portfolio` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `portfolio_extrapayment`
--

DROP TABLE IF EXISTS `portfolio_extrapayment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `portfolio_extrapayment` (
  `extraPayment_id` int(11) NOT NULL AUTO_INCREMENT,
  `portfolio_id` int(11) DEFAULT NULL,
  `extraPayment_amount` decimal(19,3) DEFAULT NULL,
  `extraPayment_date` date DEFAULT NULL,
  PRIMARY KEY (`extraPayment_id`),
  KEY `portfolio_id` (`portfolio_id`),
  CONSTRAINT `portfolio_extrapayment_ibfk_1` FOREIGN KEY (`portfolio_id`) REFERENCES `portfolio` (`portfolio_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `portfolio_extrapayment`
--

LOCK TABLES `portfolio_extrapayment` WRITE;
/*!40000 ALTER TABLE `portfolio_extrapayment` DISABLE KEYS */;
/*!40000 ALTER TABLE `portfolio_extrapayment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `portfolio_finance`
--

DROP TABLE IF EXISTS `portfolio_finance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `portfolio_finance` (
  `finance_id` int(11) NOT NULL AUTO_INCREMENT,
  `portfolio_id` int(11) DEFAULT NULL,
  `finance_amount` decimal(19,3) DEFAULT NULL,
  `finance_untill_date` date DEFAULT NULL,
  PRIMARY KEY (`finance_id`),
  KEY `finance_untill_date_idx` (`finance_untill_date`),
  KEY `portfolio_id` (`portfolio_id`),
  CONSTRAINT `portfolio_finance_ibfk_1` FOREIGN KEY (`portfolio_id`) REFERENCES `portfolio` (`portfolio_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=121 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `portfolio_finance`
--

LOCK TABLES `portfolio_finance` WRITE;
/*!40000 ALTER TABLE `portfolio_finance` DISABLE KEYS */;
INSERT INTO `portfolio_finance` VALUES (41,4,8000.000,'2014-11-01'),(92,5,2500.000,'2015-07-14'),(103,6,3000.000,'2015-12-31'),(111,5,3500.000,'2015-12-31'),(119,7,0.000,'2015-12-31'),(120,8,2000.000,'2015-09-30');
/*!40000 ALTER TABLE `portfolio_finance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project`
--

DROP TABLE IF EXISTS `project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `project` (
  `project_id` int(11) NOT NULL AUTO_INCREMENT,
  `client_id` int(11) DEFAULT NULL,
  `portfolio_id` int(11) DEFAULT NULL,
  `project_name` varchar(128) NOT NULL,
  `project_code` varchar(32) NOT NULL,
  `project_description` varchar(1024) DEFAULT NULL,
  `project_address_street` varchar(100) DEFAULT NULL,
  `project_address_city` int(11) DEFAULT NULL,
  `project_address_province` int(11) DEFAULT NULL,
  `project_address_country` int(11) DEFAULT NULL,
  `project_address_postal_code` varchar(50) DEFAULT NULL,
  `propused_start_date` date DEFAULT NULL,
  `proposed_finish_date` date DEFAULT NULL,
  `Weekend_days_id` int(11) DEFAULT NULL,
  `interest_rate` decimal(16,13) DEFAULT NULL,
  `overhead_per_day` decimal(9,3) DEFAULT NULL,
  `retained_percentage` decimal(16,13) DEFAULT NULL,
  `advanced_payment_percentage` decimal(16,13) DEFAULT NULL,
  `advanced_payment_amount` decimal(19,3) DEFAULT NULL,
  `collect_payment_period` int(11) DEFAULT NULL,
  `payment_request_period` int(11) DEFAULT NULL,
  `delay_penalty_amount` decimal(19,3) DEFAULT NULL,
  PRIMARY KEY (`project_id`),
  UNIQUE KEY `project_code` (`project_code`),
  KEY `project_name_idx` (`project_name`),
  KEY `project_code_idx` (`project_code`),
  KEY `project_address_city` (`project_address_city`),
  KEY `project_address_province` (`project_address_province`),
  KEY `project_address_country` (`project_address_country`),
  KEY `client_id` (`client_id`),
  KEY `portfolio_id` (`portfolio_id`),
  KEY `Weekend_days_id` (`Weekend_days_id`),
  CONSTRAINT `project_ibfk_1` FOREIGN KEY (`project_address_city`) REFERENCES `location_info` (`location_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `project_ibfk_2` FOREIGN KEY (`project_address_province`) REFERENCES `location_info` (`location_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `project_ibfk_3` FOREIGN KEY (`project_address_country`) REFERENCES `location_info` (`location_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `project_ibfk_4` FOREIGN KEY (`client_id`) REFERENCES `client` (`client_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `project_ibfk_5` FOREIGN KEY (`portfolio_id`) REFERENCES `portfolio` (`portfolio_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `project_ibfk_6` FOREIGN KEY (`Weekend_days_id`) REFERENCES `weekend_days` (`weekend_days_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project`
--

LOCK TABLES `project` WRITE;
/*!40000 ALTER TABLE `project` DISABLE KEYS */;
INSERT INTO `project` VALUES (6,NULL,4,'12Act - 6','proj-1','proj-1','',NULL,NULL,NULL,'','2014-08-01',NULL,NULL,0.0000000000000,100.000,0.0000000000000,0.0000000000000,0.000,10,10,0.000),(7,NULL,4,'12Act - 7','proj-2','proj-2','',NULL,NULL,NULL,'','2014-08-06',NULL,NULL,0.0000000000000,100.000,0.0000000000000,0.0000000000000,0.000,10,10,0.000),(9,NULL,5,'27Activities','27Activities','27Activities','146B Medhurst dr.',3,2,1,'K2G 5K8','2015-06-01',NULL,1,0.0000000000000,100.000,0.0500000000000,0.1000000000000,2522.000,10,10,500.000),(10,NULL,1,'test','tst','testing','',3,2,1,'',NULL,NULL,NULL,0.0000000000000,0.000,0.0000000000000,0.0000000000000,0.000,0,0,0.000),(11,NULL,6,'Proj1','Proj1','Proj1','',NULL,NULL,NULL,'',NULL,NULL,1,0.0000000000000,0.000,0.0000000000000,0.0000000000000,0.000,0,0,0.000),(12,NULL,6,'Proj2','Proj2','Proj2','',NULL,NULL,NULL,'',NULL,NULL,1,0.0000000000000,0.000,0.0000000000000,0.0000000000000,0.000,0,0,0.000),(32,NULL,7,'Project1','Project1','Project1','',NULL,NULL,NULL,'','2015-06-01',NULL,NULL,0.0000000000000,0.000,0.0000000000000,0.0000000000000,0.000,10,10,0.000),(33,NULL,7,'Project2','Project2','Project2','',NULL,NULL,NULL,'','2015-06-06',NULL,NULL,0.0000000000000,0.000,0.0000000000000,0.0000000000000,0.000,10,10,0.000),(34,NULL,8,'12 activity','12 activity','','',NULL,NULL,NULL,'',NULL,NULL,NULL,0.0000000000000,0.000,0.0000000000000,0.0000000000000,0.000,0,0,0.000);
/*!40000 ALTER TABLE `project` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_payment`
--

DROP TABLE IF EXISTS `project_payment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `project_payment` (
  `payment_id` int(11) NOT NULL AUTO_INCREMENT,
  `project_id` int(11) DEFAULT NULL,
  `payment_type_id` int(11) DEFAULT NULL,
  `payment_amount` decimal(19,3) DEFAULT NULL,
  `payment_date` date DEFAULT NULL,
  `payment_interim_number` varchar(64) DEFAULT NULL,
  `payment_initial_amount` decimal(19,3) DEFAULT NULL,
  PRIMARY KEY (`payment_id`),
  KEY `project_payment_date_idx` (`payment_date`),
  KEY `project_id` (`project_id`),
  KEY `payment_type_id` (`payment_type_id`),
  CONSTRAINT `project_payment_ibfk_1` FOREIGN KEY (`project_id`) REFERENCES `project` (`project_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `project_payment_ibfk_2` FOREIGN KEY (`payment_type_id`) REFERENCES `payment_type` (`payment_type_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=221 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_payment`
--

LOCK TABLES `project_payment` WRITE;
/*!40000 ALTER TABLE `project_payment` DISABLE KEYS */;
INSERT INTO `project_payment` VALUES (35,6,2,0.000,'2014-08-01','pay1#1',0.000),(37,6,2,0.000,'2014-08-21','pay1#3',0.000),(38,6,2,0.000,'2014-08-31','pay1#4',0.000),(41,7,2,0.000,'2014-08-06','pay2#1',0.000),(44,7,2,0.000,'2014-08-26','pay2#3',0.000),(46,7,2,0.000,'2014-09-05','pay2#4',0.000),(117,9,1,2522.000,'2015-06-01','0',2522.000),(118,9,2,0.000,'2015-06-21','dd',0.000),(119,9,2,0.000,'2015-07-01','rr',0.000),(120,9,2,0.000,'2015-07-11','ooo',0.000),(121,9,2,0.000,'2015-07-21','ppp',0.000),(122,9,2,0.000,'2015-07-31','ll',0.000),(123,9,2,0.000,'2015-08-10','opp',0.000),(124,9,2,0.000,'2015-08-20','jjj',0.000),(125,9,2,0.000,'2015-08-30','piii',0.000),(127,9,2,0.000,'2015-09-09','poiii',0.000),(128,9,2,0.000,'2015-09-19','www',0.000),(129,9,2,0.000,'2015-09-29','gfdd',0.000),(130,9,2,0.000,'2015-10-09','ooo',0.000),(133,10,2,0.000,'2015-06-11','loan#1',1000.000),(134,10,2,0.000,'2015-06-21','pay#1',0.000),(135,10,2,0.000,'2015-07-01','loan#2',1000.000),(136,10,2,0.000,'2015-07-11','pay#2',0.000),(137,10,1,750.000,'2015-06-01','0',750.000),(138,7,2,0.000,'2014-09-15','hggg',0.000),(139,6,2,0.000,'2014-09-10','dd',0.000),(153,12,2,0.000,'2015-07-01','P2',0.000),(155,12,2,0.000,'2015-07-11','P3',0.000),(156,12,2,0.000,'2015-07-21','P4',0.000),(158,12,2,0.000,'2015-07-31','P5',0.000),(160,12,2,0.000,'2015-08-10','P6',0.000),(162,12,2,0.000,'2015-08-20','P7',0.000),(163,12,2,0.000,'2015-08-30','P8',0.000),(165,12,2,0.000,'2015-09-09','P9',0.000),(166,12,2,0.000,'2015-09-19','P10',0.000),(168,12,2,0.000,'2015-09-29','P11',0.000),(171,11,2,0.000,'2015-06-21','P1',0.000),(172,11,2,0.000,'2015-07-01','P2',0.000),(173,11,2,0.000,'2015-07-11','P3',0.000),(174,11,2,0.000,'2015-07-21','P4',0.000),(175,11,2,0.000,'2015-07-31','P5',0.000),(176,11,2,0.000,'2015-08-10','P6',0.000),(177,11,2,0.000,'2015-08-20','P7',0.000),(178,11,2,0.000,'2015-08-30','P8',0.000),(179,11,2,0.000,'2015-09-09','P9',0.000),(180,11,2,0.000,'2015-09-19','P10',0.000),(181,11,2,0.000,'2015-09-29','P11',0.000),(182,11,2,0.000,'2015-06-01','Start',0.000),(183,12,2,0.000,'2015-06-06','Start',0.000),(184,12,2,0.000,'2015-06-26','P1',0.000),(185,32,2,0.000,'2015-06-21','P1',0.000),(186,32,2,0.000,'2015-07-01','P2',0.000),(187,32,2,0.000,'2015-07-11','P3',0.000),(191,32,2,0.000,'2015-07-21','P4',0.000),(192,32,2,0.000,'2015-07-31','P5',0.000),(193,32,2,0.000,'2015-08-10','P6',0.000),(195,32,2,0.000,'2015-08-30','P8',0.000),(196,32,2,0.000,'2015-09-09','P9',0.000),(197,32,2,0.000,'2015-06-01','Start',0.000),(198,33,2,0.000,'2015-06-06','Start',0.000),(199,33,2,0.000,'2015-06-26','P1',0.000),(200,33,2,0.000,'2015-07-06','P2',0.000),(202,33,2,0.000,'2015-07-16','P3',0.000),(203,33,2,0.000,'2015-07-26','P4',0.000),(204,33,2,0.000,'2015-08-05','P5',0.000),(205,33,2,0.000,'2015-08-15','P6',0.000),(208,32,2,0.000,'2015-08-20','P7',0.000),(210,33,2,0.000,'2015-08-25','P7',0.000),(211,6,2,0.000,'2014-08-11','pay2',0.000),(212,7,2,0.000,'2014-08-16','pay2',0.000),(213,34,2,0.000,'2014-05-06','p#1',0.000),(214,34,2,0.000,'2014-05-11','p#2',0.000),(215,34,2,0.000,'2014-05-16','p#3',0.000),(216,34,2,0.000,'2014-05-21','p#4',0.000),(217,34,2,0.000,'2014-05-26','p#5',0.000),(218,34,2,0.000,'2014-05-31','p#6',0.000),(219,34,2,0.000,'2014-06-05','p#7',0.000),(220,34,2,0.000,'2014-06-10','p#8',0.000);
/*!40000 ALTER TABLE `project_payment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_task`
--

DROP TABLE IF EXISTS `project_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `project_task` (
  `task_id` int(11) NOT NULL AUTO_INCREMENT,
  `project_id` int(11) DEFAULT NULL,
  `task_name` varchar(256) DEFAULT NULL,
  `task_description` varchar(1024) DEFAULT NULL,
  `duration` int(11) DEFAULT NULL,
  `calender_duration` int(11) DEFAULT NULL,
  `uniform_daily_cost` decimal(19,3) DEFAULT NULL,
  `uniform_daily_income` decimal(19,3) DEFAULT NULL,
  `tentative_start_date` date DEFAULT NULL,
  `calendar_start_date` date DEFAULT NULL,
  `scheduled_start_date` date DEFAULT NULL,
  `actual_start_date` date DEFAULT NULL,
  PRIMARY KEY (`task_id`),
  KEY `tentative_start_date_idx` (`tentative_start_date`),
  KEY `scheduled_start_date_idx` (`scheduled_start_date`),
  KEY `actual_start_date_idx` (`actual_start_date`),
  KEY `project_id` (`project_id`),
  CONSTRAINT `project_task_ibfk_1` FOREIGN KEY (`project_id`) REFERENCES `project` (`project_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=225 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
ALTER TABLE project_task
ADD CONSTRAINT uc_project_task_name UNIQUE (project_id, task_name);
--
-- Dumping data for table `project_task`
--

LOCK TABLES `project_task` WRITE;
/*!40000 ALTER TABLE `project_task` DISABLE KEYS */;
INSERT INTO `project_task` VALUES (33,6,'task1-1','1-1',4,4,100.000,200.000,'2014-08-01','2014-08-01','2014-08-01',NULL),(34,6,'task2-1','2-1',2,2,150.000,300.000,'2014-08-01','2014-08-05','2014-08-05',NULL),(35,6,'task4-1','4-1',1,1,120.000,240.000,'2014-08-01','2014-08-05','2014-08-05',NULL),(36,6,'task7-1','7-1',2,2,75.000,150.000,'2014-08-01','2014-08-07','2014-08-07',NULL),(37,6,'task3-1','3-1',3,3,50.000,100.000,'2014-08-01','2014-08-07','2014-08-07',NULL),(38,6,'task5-1','5-1',4,4,80.000,160.000,'2014-08-01','2014-08-07','2014-08-07',NULL),(39,6,'task9-1','9-1',5,5,90.000,180.000,'2014-08-01','2014-08-11','2014-08-11',NULL),(40,6,'task8-1','8-1',3,3,70.000,140.000,'2014-08-01','2014-08-11','2014-08-11',NULL),(41,6,'task6-1','6-1',1,1,60.000,120.000,'2014-08-01','2014-08-11','2014-08-11',NULL),(42,6,'task10-1','10-1',2,2,90.000,180.000,'2014-08-01','2014-08-14','2014-08-14',NULL),(43,6,'task11-1','11-1',3,3,110.000,220.000,'2014-08-01','2014-08-16','2014-08-16',NULL),(44,6,'task12-1','12-1',3,3,130.000,260.000,'2014-08-01','2014-08-19','2014-08-19',NULL),(45,7,'task1-2','1-2',1,1,100.000,200.000,'2014-08-06','2014-08-06','2014-08-06',NULL),(46,7,'task2-2','2-2',1,1,150.000,300.000,'2014-08-06','2014-08-07','2014-08-07',NULL),(47,7,'task3-2','3-2',5,5,50.000,100.000,'2014-08-06','2014-08-07','2014-08-07',NULL),(48,7,'task4-2','4-2',3,3,120.000,240.000,'2014-08-06','2014-08-08','2014-08-08',NULL),(49,7,'task5-2','5-2',2,2,80.000,160.000,'2014-08-06','2014-08-12','2014-08-12',NULL),(50,7,'task6-2','6-2',1,1,60.000,120.000,'2014-08-06','2014-08-12','2014-08-12',NULL),(51,7,'task7-2','7-2',2,2,75.000,150.000,'2014-08-06','2014-08-14','2014-08-14',NULL),(52,7,'task8-2','8-2',5,5,70.000,140.000,'2014-08-06','2014-08-21','2014-08-21',NULL),(53,7,'task9-2','9-2',4,4,90.000,180.000,'2014-08-06','2014-08-18','2014-08-18',NULL),(54,7,'task10-2','10-2',3,3,90.000,180.000,'2014-08-06','2014-08-22','2014-08-22',NULL),(55,7,'task11-2','11-2',5,5,110.000,220.000,'2014-08-06','2014-08-22','2014-08-22',NULL),(56,7,'task12-2','12-2',1,1,130.000,260.000,'2014-08-06','2014-08-27',NULL,NULL),(71,9,'ACT(ST)','ST',5,7,90.000,180.000,'2015-06-01','2015-06-05','2015-06-05',NULL),(72,9,'ACT(A)','A',5,7,50.000,100.000,'2015-06-01','2015-06-12','2015-06-12',NULL),(73,9,'ACT(B)','B',4,6,60.000,120.000,'2015-06-01','2015-06-12','2015-06-12',NULL),(74,9,'ACT(C)','C',7,11,70.000,140.000,'2015-06-01','2015-06-12','2015-06-12',NULL),(75,9,'ACT(D)','D',6,8,80.000,160.000,'2015-06-01','2015-06-12','2015-06-12',NULL),(76,9,'ACT(E)','E',7,11,55.000,110.000,'2015-06-01','2015-06-19','2015-06-19',NULL),(77,9,'ACT(F)','F',6,8,65.000,130.000,'2015-06-01','2015-06-23','2015-06-23',NULL),(78,9,'ACT(G)','G',8,10,75.000,150.000,'2015-06-01','2015-06-23','2015-06-23',NULL),(79,9,'ACT(H)','H',7,9,70.000,140.000,'2015-06-01','2015-06-23','2015-06-23',NULL),(80,9,'ACT(I)','I',6,8,85.000,170.000,'2015-06-01','2015-06-22','2015-06-22',NULL),(81,9,'ACT(J)','J',8,10,40.000,80.000,'2015-06-01','2015-07-01',NULL,NULL),(82,9,'ACT(K)','K',7,9,50.000,100.000,'2015-06-01','2015-07-01',NULL,NULL),(83,9,'ACT(L)','L',7,9,60.000,120.000,'2015-06-01','2015-07-02',NULL,NULL),(84,9,'ACT(M)','M',8,12,80.000,160.000,'2015-06-01','2015-07-02',NULL,NULL),(85,9,'ACT(N)','N',6,8,50.000,100.000,'2015-06-01','2015-07-13',NULL,NULL),(86,9,'ACT(O)','O',7,9,70.000,140.000,'2015-06-01','2015-07-13',NULL,NULL),(87,9,'ACT(P)','P',8,10,90.000,180.000,'2015-06-01','2015-07-13',NULL,NULL),(88,9,'ACT(Q)','Q',7,9,80.000,160.000,'2015-06-01','2015-07-14',NULL,NULL),(89,9,'ACT(R)','R',8,10,60.000,120.000,'2015-06-01','2015-07-21',NULL,NULL),(90,9,'ACT(S)','S',9,11,70.000,140.000,'2015-06-01','2015-07-21',NULL,NULL),(91,9,'ACT(T)','T',8,12,65.000,130.000,'2015-06-01','2015-07-23',NULL,NULL),(92,9,'ACT(U)','U',7,9,80.000,160.000,'2015-06-01','2015-07-23',NULL,NULL),(93,9,'ACT(V)','V',9,13,60.000,120.000,'2015-06-01','2015-07-23',NULL,NULL),(94,9,'ACT(W)','W',9,13,55.000,110.000,'2015-06-01','2015-07-31',NULL,NULL),(95,9,'ACT(X)','X',9,11,40.000,80.000,'2015-06-01','2015-08-04',NULL,NULL),(96,9,'ACT(Y)','Y',2,2,70.000,140.000,'2015-06-01','2015-08-03',NULL,NULL),(97,9,'ACT(FH)','FH',10,12,80.000,160.000,'2015-06-01','2015-08-17',NULL,NULL),(98,10,'Task-A','A',5,5,150.000,300.000,'2015-06-01','2015-06-01',NULL,NULL),(99,10,'Task-B','B',8,8,100.000,200.000,'2015-06-01','2015-06-06',NULL,NULL),(100,10,'Task-C','C',8,8,150.000,300.000,'2015-06-01','2015-06-14',NULL,NULL),(101,10,'Task-D','D',8,8,100.000,200.000,'2015-06-01','2015-06-22',NULL,NULL),(102,11,'ST-task','ST-1',5,5,90.000,180.000,'2015-06-01','2015-06-01',NULL,NULL),(103,11,'A-Task','A-1',5,7,50.000,100.000,'2015-06-01','2015-06-06',NULL,NULL),(104,11,'B-task','B-1',4,6,60.000,120.000,'2015-06-01','2015-06-06',NULL,NULL),(105,11,'C-task','C-1',7,11,70.000,140.000,'2015-06-01','2015-06-06',NULL,NULL),(106,11,'D-task','D-1',6,10,80.000,160.000,'2015-06-01','2015-06-06',NULL,NULL),(107,11,'E-task','E-1',7,11,55.000,110.000,'2015-06-01','2015-06-13',NULL,NULL),(108,11,'F-task','F-1',6,8,65.000,130.000,'2015-06-01','2015-06-17',NULL,NULL),(109,11,'G-task','G-1',8,10,75.000,150.000,'2015-06-01','2015-06-17',NULL,NULL),(110,11,'H-task','H-1',7,9,70.000,140.000,'2015-06-01','2015-06-17',NULL,NULL),(111,11,'I-task','I-1',6,8,85.000,170.000,'2015-06-01','2015-06-16',NULL,NULL),(112,11,'J-task','J-1',8,12,40.000,80.000,'2015-06-01','2015-06-25',NULL,NULL),(113,11,'K-task','K-1',7,9,50.000,100.000,'2015-06-01','2015-06-25',NULL,NULL),(114,11,'L-task','L-1',7,11,60.000,120.000,'2015-06-01','2015-06-26',NULL,NULL),(115,11,'M-task','M-1',8,12,80.000,160.000,'2015-06-01','2015-06-26',NULL,NULL),(116,11,'N-task','N-1',6,8,50.000,100.000,'2015-06-01','2015-07-07',NULL,NULL),(117,11,'O-task','O-1',7,9,70.000,140.000,'2015-06-01','2015-07-07',NULL,NULL),(118,11,'P-task','P-1',8,10,90.000,180.000,'2015-06-01','2015-07-07',NULL,NULL),(119,11,'Q-task','Q-1',7,9,80.000,160.000,'2015-06-01','2015-07-08',NULL,NULL),(120,11,'R-task','R-1',8,10,60.000,120.000,'2015-06-01','2015-07-15',NULL,NULL),(121,11,'S-task','S-1',9,13,70.000,140.000,'2015-06-01','2015-07-15',NULL,NULL),(122,11,'T-task','T-1',8,12,65.000,130.000,'2015-06-01','2015-07-17',NULL,NULL),(123,11,'U-task','U-1',7,11,80.000,160.000,'2015-06-01','2015-07-17',NULL,NULL),(124,11,'V-task','V-1',9,13,60.000,120.000,'2015-06-01','2015-07-17',NULL,NULL),(125,11,'W-task','W-1',9,13,55.000,110.000,'2015-06-01','2015-07-25',NULL,NULL),(126,11,'X-task','X-1',9,13,40.000,80.000,'2015-06-01','2015-07-29',NULL,NULL),(127,11,'Y-task','Y-1',2,2,70.000,140.000,'2015-06-01','2015-07-28',NULL,NULL),(128,11,'FH-task','FH-1',10,14,80.000,160.000,'2015-06-01','2015-08-11',NULL,NULL),(129,12,'ST-task','ST-2',5,7,90.000,180.000,'2015-06-06','2015-06-06',NULL,NULL),(130,12,'A-task','A-2',5,7,50.000,100.000,'2015-06-01','2015-06-13',NULL,NULL),(131,12,'B-task','B-2',4,6,60.000,120.000,'2015-06-01','2015-06-13',NULL,NULL),(132,12,'C-task','C-2',7,11,70.000,140.000,'2015-06-01','2015-06-13',NULL,NULL),(133,12,'D-task','D-2',6,10,80.000,160.000,'2015-06-01','2015-06-13',NULL,NULL),(134,12,'E-task','E-2',7,11,55.000,110.000,'2015-06-01','2015-06-20',NULL,NULL),(135,12,'F-task','F-2',6,8,65.000,130.000,'2015-06-01','2015-06-24',NULL,NULL),(136,12,'G-task','G-2',8,10,75.000,150.000,'2015-06-01','2015-06-24',NULL,NULL),(137,12,'H-task','H-2',7,9,70.000,140.000,'2015-06-01','2015-06-24',NULL,NULL),(138,12,'I-task','I-2',6,8,85.000,170.000,'2015-06-01','2015-06-23',NULL,NULL),(139,12,'J-task','J-2',8,12,40.000,80.000,'2015-06-01','2015-07-02',NULL,NULL),(140,12,'K-task','K-2',7,9,50.000,100.000,'2015-06-01','2015-07-02',NULL,NULL),(141,12,'L-task','L-2',7,11,60.000,120.000,'2015-06-01','2015-07-03',NULL,NULL),(142,12,'M-task','M-2',8,12,80.000,160.000,'2015-06-01','2015-07-03',NULL,NULL),(143,12,'N-task','N-2',6,8,50.000,100.000,'2015-06-01','2015-07-14',NULL,NULL),(144,12,'O-task','O-2',7,9,70.000,140.000,'2015-06-01','2015-07-14',NULL,NULL),(145,12,'P-task','P-2',8,10,90.000,180.000,'2015-06-01','2015-07-14',NULL,NULL),(146,12,'Q-task','Q-2',7,9,80.000,160.000,'2015-06-01','2015-07-15',NULL,NULL),(147,12,'R-task','R-2',8,10,60.000,120.000,'2015-06-01','2015-07-22',NULL,NULL),(148,12,'S-task','S-2',9,13,70.000,140.000,'2015-06-01','2015-07-22',NULL,NULL),(149,12,'T-task','T-2',8,12,65.000,130.000,'2015-06-01','2015-07-24',NULL,NULL),(150,12,'U-task','U-2',7,11,80.000,160.000,'2015-06-01','2015-07-24',NULL,NULL),(151,12,'V-task','V-2',9,13,60.000,120.000,'2015-06-01','2015-07-24',NULL,NULL),(152,12,'W-task','W-2',9,13,55.000,110.000,'2015-06-01','2015-08-01',NULL,NULL),(153,12,'X-task','X-2',9,13,40.000,80.000,'2015-06-01','2015-08-05',NULL,NULL),(154,12,'Y-task','Y-2',2,2,70.000,140.000,'2015-06-01','2015-08-04',NULL,NULL),(155,12,'FH-task','FH-2',10,14,80.000,160.000,'2015-06-01','2015-08-18',NULL,NULL),(156,32,'ST-task','ST-1',5,5,90.000,180.000,'2015-06-01','2015-06-01',NULL,NULL),(157,32,'A-task','A-1',5,5,50.000,100.000,'2015-06-01','2015-06-06',NULL,NULL),(158,32,'B-task','B-1',4,4,60.000,120.000,'2015-06-01','2015-06-06',NULL,NULL),(159,32,'C-task','C-1',7,7,70.000,140.000,'2015-06-01','2015-06-06',NULL,NULL),(160,32,'D-task','D-1',6,6,80.000,160.000,'2015-06-01','2015-06-06',NULL,NULL),(161,32,'E-task','E-1',7,7,55.000,110.000,'2015-06-01','2015-06-11',NULL,NULL),(162,32,'F-task','F-1',6,6,65.000,130.000,'2015-06-01','2015-06-13',NULL,NULL),(163,32,'G-task','G-1',8,8,75.000,150.000,'2015-06-01','2015-06-13',NULL,NULL),(164,32,'H-task','H-1',7,7,70.000,140.000,'2015-06-01','2015-06-13',NULL,NULL),(165,32,'I-task','I-1',6,6,85.000,170.000,'2015-06-01','2015-06-12',NULL,NULL),(166,32,'J-task','J-1',8,8,40.000,80.000,'2015-06-01','2015-06-19',NULL,NULL),(167,32,'K-task','K-1',7,7,50.000,100.000,'2015-06-01','2015-06-19',NULL,NULL),(168,32,'L-task','L-1',7,7,60.000,120.000,'2015-06-01','2015-06-20',NULL,NULL),(169,32,'M-task','M-1',8,8,80.000,160.000,'2015-06-01','2015-06-20',NULL,NULL),(170,32,'N-task','N-1',6,6,50.000,100.000,'2015-06-01','2015-06-27',NULL,NULL),(171,32,'O-task','O-1',7,7,70.000,140.000,'2015-06-01','2015-06-27',NULL,NULL),(172,32,'P-task','P-1',8,8,90.000,180.000,'2015-06-01','2015-06-27',NULL,NULL),(173,32,'Q-task','Q-1',7,7,80.000,160.000,'2015-06-01','2015-06-28',NULL,NULL),(174,32,'R-task','R-1',8,8,60.000,120.000,'2015-06-01','2015-07-03',NULL,NULL),(175,32,'S-task','S-1',9,9,70.000,140.000,'2015-06-01','2015-07-03',NULL,NULL),(176,32,'T-task','T-1',8,8,65.000,130.000,'2015-06-01','2015-07-05',NULL,NULL),(177,32,'U-task','U-1',7,7,80.000,160.000,'2015-06-01','2015-07-05',NULL,NULL),(178,32,'V-task','V-1',9,9,60.000,120.000,'2015-06-01','2015-07-05',NULL,NULL),(179,32,'W-task','W-1',9,9,55.000,110.000,'2015-06-01','2015-07-11',NULL,NULL),(180,32,'X-task','X-1',9,9,40.000,80.000,'2015-06-01','2015-07-13',NULL,NULL),(181,32,'Y-task','Y-1',2,2,70.000,140.000,'2015-06-01','2015-07-12',NULL,NULL),(182,32,'FH-task','FH-1',10,10,80.000,160.000,'2015-06-01','2015-07-22',NULL,NULL),(183,33,'ST-task','ST-2',5,5,90.000,180.000,'2015-06-06','2015-06-06',NULL,NULL),(184,33,'A-task','A-2',5,5,50.000,100.000,'2015-06-01','2015-06-11',NULL,NULL),(185,33,'B-task','B-2',4,4,60.000,120.000,'2015-06-01','2015-06-11',NULL,NULL),(186,33,'C-task','C-2',7,7,70.000,140.000,'2015-06-01','2015-06-11',NULL,NULL),(187,33,'D-task','D-2',6,6,80.000,160.000,'2015-06-01','2015-06-11',NULL,NULL),(188,33,'E-task','E-2',7,7,55.000,110.000,'2015-06-01','2015-06-16',NULL,NULL),(189,33,'F-task','F-2',6,6,65.000,130.000,'2015-06-01','2015-06-18',NULL,NULL),(190,33,'G-task','G-2',8,8,75.000,150.000,'2015-06-01','2015-06-18',NULL,NULL),(191,33,'H-task','H-2',7,7,70.000,140.000,'2015-06-01','2015-06-18',NULL,NULL),(192,33,'I-task','I-2',6,6,85.000,170.000,'2015-06-01','2015-06-17',NULL,NULL),(193,33,'J-task','J-2',8,8,40.000,80.000,'2015-06-01','2015-06-24',NULL,NULL),(194,33,'K-task','K-2',7,7,50.000,100.000,'2015-06-01','2015-06-24',NULL,NULL),(195,33,'L-task','L-2',7,7,60.000,120.000,'2015-06-01','2015-06-25',NULL,NULL),(196,33,'M-task','M-2',8,8,80.000,160.000,'2015-06-01','2015-06-25',NULL,NULL),(197,33,'N-task','N-2',6,6,50.000,100.000,'2015-06-01','2015-07-02',NULL,NULL),(198,33,'O-task','O-2',7,7,70.000,140.000,'2015-06-01','2015-07-02',NULL,NULL),(199,33,'P-task','P-2',8,8,90.000,180.000,'2015-06-01','2015-07-02',NULL,NULL),(200,33,'Q-task','Q-2',7,7,80.000,160.000,'2015-06-01','2015-07-03',NULL,NULL),(201,33,'R-task','R-2',8,8,60.000,120.000,'2015-06-01','2015-07-08',NULL,NULL),(202,33,'S-task','S-2',9,9,70.000,140.000,'2015-06-01','2015-07-08',NULL,NULL),(203,33,'T-task','T-2',8,8,65.000,130.000,'2015-06-01','2015-07-10',NULL,NULL),(204,33,'U-task','U-2',7,7,80.000,160.000,'2015-06-01','2015-07-10',NULL,NULL),(205,33,'V-task','V-2',9,9,60.000,120.000,'2015-06-01','2015-07-10',NULL,NULL),(206,33,'W-task','W-2',9,9,55.000,110.000,'2015-06-01','2015-07-16',NULL,NULL),(207,33,'X-task','X-2',9,9,40.000,80.000,'2015-06-01','2015-07-18',NULL,NULL),(210,33,'Y-task','Y-2',2,2,70.000,140.000,'2015-06-01','2015-07-17',NULL,NULL),(211,33,'FT-task','FT-2',10,10,80.000,160.000,'2015-06-01','2015-07-27',NULL,NULL),(212,34,'task-1','1',4,4,100.000,200.000,'2014-05-01','2014-05-01',NULL,NULL),(213,34,'task-2','2',2,2,150.000,300.000,'2014-05-01','2014-05-05',NULL,NULL),(215,34,'task-3','3',3,3,50.000,100.000,'2014-05-01','2014-05-07',NULL,NULL),(216,34,'task-4','4',1,1,120.000,240.000,'2014-05-01','2014-05-05',NULL,NULL),(217,34,'task-5','5',4,4,80.000,160.000,'2014-05-01','2014-05-07',NULL,NULL),(218,34,'task-6','6',1,1,60.000,120.000,'2014-05-01','2014-05-11',NULL,NULL),(219,34,'task-7','7',2,2,75.000,150.000,'2014-05-01','2014-05-07',NULL,NULL),(220,34,'task-8','8',3,3,70.000,140.000,'2014-05-01','2014-05-11',NULL,NULL),(221,34,'task-9','9',5,5,90.000,180.000,'2014-05-01','2014-05-11',NULL,NULL),(222,34,'task-10','10',2,2,90.000,180.000,'2014-05-01','2014-05-14',NULL,NULL),(223,34,'task-11','11',3,3,110.000,220.000,'2014-05-01','2014-05-16',NULL,NULL),(224,34,'task-12','12',3,3,130.000,260.000,'2014-05-01','2014-05-19',NULL,NULL);
/*!40000 ALTER TABLE `project_task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task_dependency`
--

DROP TABLE IF EXISTS `task_dependency`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `task_dependency` (
  `dependency_id` int(11) NOT NULL AUTO_INCREMENT,
  `dependant_task_id` int(11) DEFAULT NULL,
  `dependency_task_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`dependency_id`),
  UNIQUE KEY `dependant_task_id` (`dependant_task_id`,`dependency_task_id`),
  KEY `dependency_task_id` (`dependency_task_id`),
  CONSTRAINT `task_dependency_ibfk_1` FOREIGN KEY (`dependant_task_id`) REFERENCES `project_task` (`task_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `task_dependency_ibfk_2` FOREIGN KEY (`dependency_task_id`) REFERENCES `project_task` (`task_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=291 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task_dependency`
--

LOCK TABLES `task_dependency` WRITE;
/*!40000 ALTER TABLE `task_dependency` DISABLE KEYS */;
INSERT INTO `task_dependency` VALUES (29,34,33),(30,35,33),(31,36,34),(32,36,35),(33,37,34),(34,38,34),(35,38,35),(36,39,36),(37,39,37),(38,39,38),(39,40,36),(40,40,38),(41,41,38),(42,42,40),(43,43,39),(44,43,42),(45,44,41),(46,44,43),(47,46,45),(48,47,45),(49,48,46),(50,49,47),(51,50,47),(52,50,48),(53,51,48),(54,51,49),(55,52,50),(56,52,51),(57,53,51),(58,54,53),(59,55,53),(60,56,52),(61,56,54),(62,56,55),(68,72,71),(69,73,71),(70,74,71),(71,75,71),(72,76,72),(73,77,73),(74,77,74),(75,78,74),(76,79,74),(77,79,75),(78,80,75),(79,81,76),(80,81,77),(81,82,77),(82,83,79),(83,84,79),(84,84,80),(85,85,81),(87,86,78),(86,86,82),(88,86,83),(89,87,83),(90,88,83),(91,88,84),(92,89,85),(93,90,82),(94,90,85),(95,91,86),(96,91,87),(97,92,87),(98,93,88),(99,94,89),(100,95,90),(101,95,91),(102,95,92),(103,96,92),(107,97,93),(104,97,94),(105,97,95),(106,97,96),(109,99,98),(110,100,99),(111,101,100),(112,103,102),(113,104,102),(114,105,102),(115,106,102),(116,107,103),(117,108,104),(118,108,105),(119,109,105),(120,110,105),(121,110,106),(122,111,106),(123,112,107),(124,112,108),(125,113,108),(126,114,110),(127,115,110),(128,115,111),(129,116,112),(130,117,109),(131,117,113),(132,117,114),(133,118,114),(134,119,114),(135,119,115),(136,120,116),(137,121,113),(138,121,116),(139,122,117),(140,122,118),(141,123,118),(142,124,119),(143,125,120),(144,126,121),(145,126,122),(146,126,123),(147,127,123),(148,128,125),(149,128,126),(150,128,127),(151,130,129),(152,131,129),(153,132,129),(154,133,129),(155,134,130),(156,135,131),(157,135,132),(158,136,132),(159,137,132),(160,137,133),(161,138,133),(162,139,134),(163,139,135),(164,140,135),(165,141,137),(166,142,137),(167,142,138),(168,143,139),(169,144,136),(170,144,140),(171,144,141),(172,145,141),(173,146,141),(174,146,142),(175,147,143),(177,148,140),(176,148,143),(178,149,144),(179,149,145),(180,150,145),(181,151,146),(182,152,147),(183,153,148),(184,153,149),(185,153,150),(186,154,150),(187,155,151),(188,155,152),(189,155,153),(190,155,154),(191,157,156),(192,158,156),(193,159,156),(194,160,156),(195,161,157),(196,162,158),(197,162,159),(198,163,159),(199,164,159),(200,164,160),(201,165,160),(202,166,161),(203,166,162),(204,167,162),(205,168,164),(206,169,164),(207,169,165),(208,170,166),(209,171,163),(210,171,167),(211,171,168),(212,172,168),(213,173,168),(214,173,169),(215,174,170),(217,175,167),(216,175,170),(218,176,171),(219,176,172),(220,177,172),(221,178,173),(222,179,174),(223,180,175),(224,180,176),(225,180,177),(226,181,177),(230,182,178),(227,182,179),(228,182,180),(229,182,181),(231,184,183),(232,185,183),(233,186,183),(234,187,183),(235,188,184),(271,189,185),(272,189,186),(236,190,186),(237,191,186),(238,191,187),(239,192,187),(240,193,188),(241,193,189),(242,194,189),(243,195,191),(244,196,191),(245,196,192),(246,197,193),(247,198,190),(248,198,194),(249,198,195),(250,199,195),(251,200,195),(252,200,196),(253,201,197),(255,202,194),(254,202,197),(256,203,198),(257,203,199),(258,204,199),(259,205,200),(260,206,201),(261,207,202),(262,207,203),(263,207,204),(266,210,204),(269,211,205),(267,211,206),(268,211,207),(270,211,210),(273,213,212),(277,215,213),(274,216,212),(278,217,213),(279,217,216),(285,218,217),(275,219,213),(276,219,216),(283,220,217),(284,220,219),(280,221,215),(281,221,217),(282,221,219),(286,222,220),(287,223,221),(288,223,222),(289,224,218),(290,224,223);
/*!40000 ALTER TABLE `task_dependency` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `weekend_days`
--

DROP TABLE IF EXISTS `weekend_days`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `weekend_days` (
  `weekend_days_id` int(11) NOT NULL AUTO_INCREMENT,
  `weekend_days` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`weekend_days_id`),
  KEY `weekend_days_idx` (`weekend_days`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `weekend_days`
--

LOCK TABLES `weekend_days` WRITE;
/*!40000 ALTER TABLE `weekend_days` DISABLE KEYS */;
INSERT INTO `weekend_days` VALUES (2,'FRI-SAT'),(1,'SAT-SUN'),(3,'THU-FRI');
/*!40000 ALTER TABLE `weekend_days` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2015-09-13 17:12:08
