-- phpMyAdmin SQL Dump
-- version 4.6.6deb4+deb9u2
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Czas generowania: 18 Sty 2021, 18:06
-- Wersja serwera: 10.1.47-MariaDB-0+deb9u1
-- Wersja PHP: 7.0.33-0+deb9u10

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Baza danych: `venAuth`
--

DELIMITER $$
--
-- Procedury
--
CREATE PROCEDURE `newCheckUser` (IN `IN_ip` INT, IN `in_nick` TEXT CHARSET utf8, OUT `outCode` INT)  BEGIN
	DECLARE users INT;
        SET users = (SELECT COUNT(*) FROM acceptedIP WHERE `ip` = IN_ip AND data >= NOW() - interval 2 hour);
    IF users = 0 THEN
    	    	DELETE FROM acceptedIP WHERE `ip` = IN_ip;
    
    	    	SET users = (SELECT COUNT(*) FROM request WHERE `ip` = IN_ip);
    	IF users = 0 THEN
			        	SET users = (SELECT COUNT(*) FROM banList WHERE `ip` = IN_ip);
        	IF users = 0 THEN
        		                SET users = (SELECT COUNT(*) FROM tempBan WHERE `ip` = IN_ip);
                IF users = 0 THEN
                	                    SET outCode = 0;
                    INSERT INTO request (`ip`, `date`, `nick`) VALUES (IN_ip, CURRENT_TIMESTAMP,in_nick);
                ELSE
                	                    SET outCode = 1;
                END IF;
        	ELSE
        		            	SET outCode = 1;
        	END IF;
        ELSE
                    SET outCode = 1;
        END IF;
    
    ELSE 
        SET outCode = 2;
    END IF;
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Struktura tabeli dla tabeli `acceptedIP`
--

CREATE TABLE `acceptedIP` (
  `ip` int(10) UNSIGNED NOT NULL,
  `data` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Struktura tabeli dla tabeli `banList`
--

CREATE TABLE `banList` (
  `ip` int(10) UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Struktura tabeli dla tabeli `request`
--

CREATE TABLE `request` (
  `ip` int(10) UNSIGNED NOT NULL,
  `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `nick` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Struktura tabeli dla tabeli `tempBan`
--

CREATE TABLE `tempBan` (
  `ip` int(10) UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Indeksy dla zrzutów tabel
--

--
-- Indexes for table `acceptedIP`
--
ALTER TABLE `acceptedIP`
  ADD PRIMARY KEY (`ip`);

--
-- Indexes for table `banList`
--
ALTER TABLE `banList`
  ADD PRIMARY KEY (`ip`);

--
-- Indexes for table `request`
--
ALTER TABLE `request`
  ADD PRIMARY KEY (`ip`);

--
-- Indexes for table `tempBan`
--
ALTER TABLE `tempBan`
  ADD PRIMARY KEY (`ip`);

DELIMITER $$
--
-- Zdarzenia
--
CREATE EVENT `temp_clear` ON SCHEDULE EVERY 1 DAY STARTS '2021-01-01 00:01:00' ON COMPLETION PRESERVE ENABLE DO DELETE FROM tempBan$$

DELIMITER ;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
