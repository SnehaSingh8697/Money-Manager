/*******************************************************************************
   Drop database if it exists
********************************************************************************/

DROP USER dfs CASCADE;


/*******************************************************************************
   Create database
********************************************************************************/
CREATE USER dfs
IDENTIFIED BY dfsboss
DEFAULT TABLESPACE users
TEMPORARY TABLESPACE temp
QUOTA 10M ON users;

GRANT connect to dfs;
GRANT resource to dfs;
GRANT create session TO dfs;
GRANT create sequence to dfs;
GRANT create role to dfs;
GRANT create procedure to dfs;
GRANT create type to dfs;
GRANT create trigger to dfs;
GRANT create table TO dfs;
GRANT create view TO dfs;


connect dfs/dfsboss



create sequence seq start with 1 increment by 1;

/*******************************************************************************
   Create Tables
********************************************************************************/

Create table HolidayPic(
	Id Number not null primary key,
	Name varchar2(30) unique,
	Pic Blob,
	Regular char(1)
);

Create table RegularHoliday(
	Id Number not null primary key,
	Month int,
	Day int,
	HPicId Number not null,
    FOREIGN KEY(HPicId) references HolidayPic(Id)
);

Create table IrregularHoliday(
	Id Number not null primary key,
	Dt Date,
	HPicId Number not null,
    FOREIGN KEY(HPicId) references HolidayPic(Id)
);
Create table Summary(
	dt Date unique,
	Income Number(9),
	Expense Number(9)

);

Insert into Summary(dt,Income,Expense) values('20-MAR-2019',0,2000);
Insert into Summary(dt,Income,Expense) values('11-MAR-2019',100,300);
Insert into Summary(dt,Income,Expense) values('28-MAR-2019',1000,3000);