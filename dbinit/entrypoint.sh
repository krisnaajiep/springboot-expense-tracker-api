#!/bin/bash

# Start the script to create the DB and user
/opt/mssql-tools/bin/sqlcmd -S "$1" -U "$2" -P "$3" -N -C -Q "
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'$4')
BEGIN
    CREATE DATABASE [$4];
END
GO"


