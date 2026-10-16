CREATE DATABASE  IF NOT EXISTS `campus_ict_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `campus_ict_db`;
-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: campus_ict_db
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `technicians`
--

DROP TABLE IF EXISTS `technicians`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `technicians` (
  `tech_id` int NOT NULL AUTO_INCREMENT,
  `tech_name` varchar(100) NOT NULL,
  `department` varchar(100) NOT NULL DEFAULT 'ICT Support',
  `email` varchar(100) NOT NULL,
  `status` enum('Available','Busy','On Leave') NOT NULL DEFAULT 'Available',
  PRIMARY KEY (`tech_id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `technicians`
--

LOCK TABLES `technicians` WRITE;
/*!40000 ALTER TABLE `technicians` DISABLE KEYS */;
INSERT INTO `technicians` VALUES (6,'Alex Example','ICT Support','alex.example@example.com','Available'),(7,'Jordan Demo','ICT Support','jordan.demo@example.com','Busy');
/*!40000 ALTER TABLE `technicians` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tickets`
--

DROP TABLE IF EXISTS `tickets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tickets` (
  `ticket_id` int NOT NULL AUTO_INCREMENT,
  `reporter_id` int NOT NULL,
  `category` enum('Wi-Fi / Network','Hardware','Software','Lab Equipment','Other') NOT NULL,
  `location` varchar(100) NOT NULL,
  `description` text NOT NULL,
  `urgency` enum('Low','Medium','High','Critical') NOT NULL DEFAULT 'Medium',
  `status` enum('Pending','In Progress','Resolved') NOT NULL DEFAULT 'Pending',
  `assigned_tech_id` int DEFAULT NULL,
  `resolution_notes` text,
  `resolved_at` datetime DEFAULT NULL,
  `date_reported` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ticket_id`),
  KEY `fk_ticket_reporter` (`reporter_id`),
  KEY `fk_ticket_technician` (`assigned_tech_id`),
  CONSTRAINT `fk_ticket_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_ticket_technician` FOREIGN KEY (`assigned_tech_id`) REFERENCES `technicians` (`tech_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tickets`
--

LOCK TABLES `tickets` WRITE;
/*!40000 ALTER TABLE `tickets` DISABLE KEYS */;
INSERT INTO `tickets` VALUES (4,4,'Wi-Fi / Network','Library Study Area','Demonstration wireless connectivity issue.','High','Pending',NULL,NULL,NULL,'2026-10-10 09:18:42','2026-10-10 09:18:42'),(5,5,'Lab Equipment','Computer Lab A','Demonstration network-switch issue.','Critical','In Progress',7,NULL,NULL,'2026-10-10 09:19:36','2026-10-12 10:36:00');
/*!40000 ALTER TABLE `tickets` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `full_name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `role` enum('Student','Staff') NOT NULL DEFAULT 'Student',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (4,'Avery Student','avery.student@example.com','+1-202-555-0101','Student'),(5,'Morgan Staff','morgan.staff@example.com','+1-202-555-0102','Staff');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-14  8:45:30
