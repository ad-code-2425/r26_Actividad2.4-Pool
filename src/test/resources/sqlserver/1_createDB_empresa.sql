USE [master]
GO

IF NOT EXISTS (  SELECT * FROM sys.databases  WHERE name = 'empresatest')
BEGIN

CREATE DATABASE [empresatest];
END