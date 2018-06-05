CREATE DATABASE  IF NOT EXISTS `LOGIC_BOMB_ANALYSIS_DB` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `LOGIC_BOMB_ANALYSIS_DB`;
-- MySQL dump 10.13  Distrib 5.7.22, for Linux (x86_64)
--
-- Host: localhost    Database: LOGIC_BOMB_ANALYSIS_DB
-- ------------------------------------------------------
-- Server version	5.7.22-0ubuntu0.16.04.1

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
-- Table structure for table `REF_APPLICATION`
--

DROP TABLE IF EXISTS `REF_APPLICATION`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `REF_APPLICATION` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(150) CHARACTER SET utf8 DEFAULT NULL,
  `NUM_METHODS` int(11) DEFAULT NULL,
  `NUM_METHODS_WITH_COND_USED` int(11) DEFAULT NULL,
  `NUM_BLOCKS` int(11) DEFAULT NULL,
  `NUM_BLOCKS_WITH_COND_USED` int(11) DEFAULT NULL,
  `IS_MALICIOUS` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `NAME_UNIQUE` (`NAME`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `REF_APPLICATION`
--

LOCK TABLES `REF_APPLICATION` WRITE;
/*!40000 ALTER TABLE `REF_APPLICATION` DISABLE KEYS */;
INSERT INTO `REF_APPLICATION` VALUES (3,'com.mike.test.apk',0,1,5,9,12);
/*!40000 ALTER TABLE `REF_APPLICATION` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `REF_BRANCH`
--

DROP TABLE IF EXISTS `REF_BRANCH`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `REF_BRANCH` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `METHOD_ID` int(11) DEFAULT NULL,
  `COND_BLOCK_ID` int(11) DEFAULT NULL,
  `NAME` varchar(150) CHARACTER SET utf8 DEFAULT NULL,
  `NUM_UNITS` int(11) DEFAULT NULL,
  `NUM_COND_VAR_USAGES` int(11) DEFAULT NULL,
  `NUM_NON_COND_VAR` int(11) DEFAULT NULL,
  `NUM_ASSIGNMENTS` int(11) DEFAULT NULL,
  `NUM_TOTAL_VAR_USAGES` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `fk_REF_BRANCH_1_idx` (`COND_BLOCK_ID`),
  KEY `fk_REF_BRANCH_2_idx` (`METHOD_ID`),
  CONSTRAINT `fk_REF_BRANCH_1` FOREIGN KEY (`COND_BLOCK_ID`) REFERENCES `REF_COND_BLOCK` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_REF_BRANCH_2` FOREIGN KEY (`METHOD_ID`) REFERENCES `REF_METHOD` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `REF_BRANCH`
--

LOCK TABLES `REF_BRANCH` WRITE;
/*!40000 ALTER TABLE `REF_BRANCH` DISABLE KEYS */;
INSERT INTO `REF_BRANCH` VALUES (7,4,3,'branch 1',0,1,2,3,4),(8,4,3,'branch 2',0,1,2,3,4);
/*!40000 ALTER TABLE `REF_BRANCH` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `REF_COND_BLOCK`
--

DROP TABLE IF EXISTS `REF_COND_BLOCK`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `REF_COND_BLOCK` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `METHOD_ID` int(11) DEFAULT NULL,
  `NAME` varchar(150) CHARACTER SET utf8 DEFAULT NULL,
  `CONDITION_NAME` varchar(150) CHARACTER SET utf8 DEFAULT NULL,
  `NUM_UNITS` int(11) DEFAULT NULL,
  `NUM_ASSIGNMENTS` int(11) DEFAULT NULL,
  `NUM_COND_VAR_USAGES` int(11) DEFAULT NULL,
  `NUM_TOTAL_USAGES` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `fk_REF_COND_BLOCK_1_idx` (`METHOD_ID`),
  CONSTRAINT `fk_REF_COND_BLOCK_1` FOREIGN KEY (`METHOD_ID`) REFERENCES `REF_METHOD` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `REF_COND_BLOCK`
--

LOCK TABLES `REF_COND_BLOCK` WRITE;
/*!40000 ALTER TABLE `REF_COND_BLOCK` DISABLE KEYS */;
INSERT INTO `REF_COND_BLOCK` VALUES (3,4,'block 1','x == y',0,2,4,0);
/*!40000 ALTER TABLE `REF_COND_BLOCK` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `REF_METHOD`
--

DROP TABLE IF EXISTS `REF_METHOD`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `REF_METHOD` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(150) CHARACTER SET utf8 DEFAULT NULL,
  `APPLICATION_ID` int(11) DEFAULT NULL,
  `NUM_DECLARED_VAR_IN_METHOD` int(11) DEFAULT NULL,
  `NUM_VAR_USED_IN_METHOD` int(11) DEFAULT NULL,
  `NUM_BLOCKS_WITH_COND_VAR_USED` int(11) DEFAULT NULL,
  `DELETED` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `fk_REF_METHOD_1_idx` (`APPLICATION_ID`),
  CONSTRAINT `fk_REF_METHOD_1` FOREIGN KEY (`APPLICATION_ID`) REFERENCES `REF_APPLICATION` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `REF_METHOD`
--

LOCK TABLES `REF_METHOD` WRITE;
/*!40000 ALTER TABLE `REF_METHOD` DISABLE KEYS */;
INSERT INTO `REF_METHOD` VALUES (4,'testmethod',3,0,0,100,NULL);
/*!40000 ALTER TABLE `REF_METHOD` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `TEST_TABLE`
--

DROP TABLE IF EXISTS `TEST_TABLE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TEST_TABLE` (
  `ID` int(11) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `TEST_TABLE`
--

LOCK TABLES `TEST_TABLE` WRITE;
/*!40000 ALTER TABLE `TEST_TABLE` DISABLE KEYS */;
/*!40000 ALTER TABLE `TEST_TABLE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'LOGIC_BOMB_ANALYSIS_DB'
--
/*!50003 DROP PROCEDURE IF EXISTS `CREATE_APPLICATION` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `CREATE_APPLICATION`(
    IN APPLICATION_NAME NVARCHAR(150), 
    IN NUM_METHODS INT,
    IN NUM_METHODS_WITH_COND_USED INT,
    IN NUM_BLOCKS INT,
    IN NUM_BLOCKS_WITH_COND_USED INT,
    IN IS_MALICIOUS INT
)
BEGIN
    SELECT 
		@NEW_ID := `AUTO_INCREMENT`
	FROM  INFORMATION_SCHEMA.TABLES
	WHERE TABLE_SCHEMA = 'LOGIC_BOMB_ANALYSIS_DB'
	AND   TABLE_NAME   = 'REF_APPLICATION';
    
    IF NOT EXISTS (SELECT 1 FROM REF_APPLICATION WHERE NAME = APPLICATION_NAME) THEN
		INSERT INTO REF_APPLICATION
		(
			ID,
			NAME,
			NUM_METHODS,
			NUM_METHODS_WITH_COND_USED,
			NUM_BLOCKS,
			NUM_BLOCKS_WITH_COND_USED,
			IS_MALICIOUS
		)
		VALUES
		(
			@NEW_ID,
			APPLICATION_NAME,
			NUM_METHODS,
			NUM_METHODS_WITH_COND_USED,
			NUM_BLOCKS,
			NUM_BLOCKS_WITH_COND_USED,
			IS_MALICIOUS
		);
	END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `CREATE_BRANCH` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `CREATE_BRANCH`(
    IN methodName NVARCHAR(150),
	IN condBlockName NVARCHAR(150),
    IN branchName NVARCHAR(150),
    IN numUnits INT,
    IN numCondVarUsages INT,
    IN numNonCondVar INT,
    IN numAssignments INT,
    IN numTotalVarUsages INT
)
BEGIN
	SELECT 
		@METHOD_ID := ID
	FROM REF_METHOD
	WHERE NAME = methodName;

	SELECT 
		@COND_BLOCK_ID := ID
	FROM REF_COND_BLOCK
	WHERE NAME = condBlockName;
		
    IF (@METHOD_ID IS NOT NULL AND @COND_BLOCK_ID IS NOT NULL) THEN
		IF NOT EXISTS (
			SELECT 1 FROM REF_BRANCH WHERE NAME = branchName AND METHOD_ID = @METHOD_ID AND COND_BLOCK_ID = @COND_BLOCK_ID
		) THEN
			SELECT 
				@NEW_ID := `AUTO_INCREMENT`
			FROM  INFORMATION_SCHEMA.TABLES
			WHERE TABLE_SCHEMA = 'LOGIC_BOMB_ANALYSIS_DB'
			AND   TABLE_NAME   = 'REF_BRANCH';
			
			INSERT INTO REF_BRANCH
			(
				ID,
				COND_BLOCK_ID,
                METHOD_ID,
				NAME,
				NUM_UNITS,
				NUM_COND_VAR_USAGES,
				NUM_NON_COND_VAR,
				NUM_ASSIGNMENTS,
				NUM_TOTAL_VAR_USAGES
			)
			VALUES
			(
				@NEW_ID,
				@COND_BLOCK_ID,
                @METHOD_ID,
				branchName,
				numUnits,
				numCondVarUsages,
				numNonCondVar,
				numAssignments,
				numTotalVarUsages
			);
		END IF;    
	END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `CREATE_COND_BLOCK` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `CREATE_COND_BLOCK`(
	IN methodName NVARCHAR(150),
    IN condBlockName NVARCHAR(150),
    IN conditionName NVARCHAR(150),
    IN numUnits INT,
    IN numAssignments INT,
    IN numCondVarUsages INT,
    IN numTotalUsages INT
)
BEGIN
	IF EXISTS (
		SELECT 1 FROM REF_METHOD WHERE NAME = methodName
    ) THEN
		SELECT 
			@METHOD_ID := ID
		FROM REF_METHOD
        WHERE NAME = methodName;
        
        IF NOT EXISTS (
			SELECT 1 FROM REF_COND_BLOCK WHERE NAME = condBlockName AND METHOD_ID = @METHOD_ID
        ) THEN
			SELECT
				@NEW_ID := `AUTO_INCREMENT`
			FROM  INFORMATION_SCHEMA.TABLES
			WHERE TABLE_SCHEMA = 'LOGIC_BOMB_ANALYSIS_DB'
			AND   TABLE_NAME   = 'REF_COND_BLOCK';
			
            INSERT INTO REF_COND_BLOCK
            (
				ID,
                METHOD_ID,
                NAME,
                CONDITION_NAME,
                NUM_UNITS,
                NUM_ASSIGNMENTS,
                NUM_COND_VAR_USAGES,
                NUM_TOTAL_USAGES
            )
            VALUES
            (
				@NEW_ID,
                @METHOD_ID,
                condBlockName,
                conditionName,
                numUnits,
                numAssignments,
                numCondVarUsages,
                numTotalUsages
            );
        END IF;
	END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `CREATE_METHOD` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `CREATE_METHOD`(
	IN applicationName NVARCHAR(150),
    IN methodName NVARCHAR(150),
    IN numDeclaredVarInMethod INT,
    IN numVarUsedInMethod INT,
    IN numBlocksWithCondVarUsed INT
)
BEGIN
	IF EXISTS (
		SELECT 1 FROM REF_APPLICATION WHERE NAME = applicationName
    ) THEN
		IF NOT EXISTS (
			SELECT 1 FROM REF_METHOD WHERE NAME = methodName
        ) THEN
			SELECT 
				@NEW_ID := `AUTO_INCREMENT`
			FROM  INFORMATION_SCHEMA.TABLES
			WHERE TABLE_SCHEMA = 'LOGIC_BOMB_ANALYSIS_DB'
			AND   TABLE_NAME   = 'REF_METHOD';
			
			SELECT
				@APPLICATION_ID := ID
			FROM REF_APPLICATION
			WHERE NAME = applicationName;
			
			INSERT INTO REF_METHOD 
			(
				ID,
				NAME,
				APPLICATION_ID,
				NUM_DECLARED_VAR_IN_METHOD,
				NUM_VAR_USED_IN_METHOD,
				NUM_BLOCKS_WITH_COND_VAR_USED
			)
			VALUES
			(
				@NEW_ID,
				methodName,
				@APPLICATION_ID,
				numDeclaredVarInMethod,
				numVarUsedInMethod,
				numBlocksWithCondVarUsed
			);
		END IF;
    END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-06-05  2:31:25
