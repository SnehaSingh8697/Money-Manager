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

Create table Wallet(
    Id Number not null primary key,
	Name varchar2(30),
	Liquidbal Number(9),
	Digitalbal Number(9),
	Pic Blob
);
Create table UserSettings(
	Dc char(1),
	LiquidWarningBalance Number(9),
	DigitalWarningBalance Number(9),
	PeriodType char(1),
	Freq1 Number(5),
	Freq2 Number(5),
	Weekday Number(1),
	PdIntervalNumber Number(9),
	PdIntervalType char(1),
	StartHomeWith char(1),
	LIQUIDWARNINGPERCENT Number(2),
	DIGITALWARNINGPERCENT Number(2),
	MIDMONTHDATE NUMBER(2)
);
Create table SystemSettings(
	OPENINGBANKBALANCE Number(9),
	OPENINGCASHBALANCE Number(9),
	REFLIQBALANCE Number(9),
	REFDIGITBALANCE Number(9),
	HOLIDAYUPDATIONDATE Date
);
Create table Pd(
	Id Number not null primary key,
	Num Number,
	Dt Date,
	NoOfSeconds int,
	BankWallletOld Number(9),
	OtherDigWalletOld Number(9),
	CashWalletOld Number(9),
	OtherCashWalletOld Number(9),
	BankWalletNew Number(9),
	OtherDigWalletNew Number(9),
	CashWalletNew Number(9),
	OtherCashWalletNew Number(9)
);
Create table PdDetails(
	PdId Number not null,
	WalletId Number not null,
	WalletDgtAmtOld Number(9),
	WalletDgtAmtNew Number(9),
	WalletLiqAmtOld Number(9),
	WalletLiqAmtNew Number(9),
    FOREIGN KEY(PdId) references Pd(Id),
	FOREIGN KEY(WalletId) references Wallet(Id),
	PRIMARY KEY(PdId,WalletId)
);
Create table IntTrans(
	Id Number not null primary key,
	Dt Date,
	NoOfSeconds int, -- time in no of seconds--
	SourceWalletId Number,
	TargetWalletId Number,
	SourceWalletOldDgtAmt Number(9),
	SourceWalletOldLiqAmt Number(9),
	TargetWalletOldDgtAmt Number(9),
	TargetWalletOldLiqAmt Number(9),
	TransDgtAmt Number(9),
	TransLiqAmt Number(9)
);
Create table PP(
	Id Number not null primary key,
	Name varchar2(30)
);
Create table ExtTrans(
	Id Number not null primary key,
	Dt Date,
	NoOfSeconds int,
	PPId Number not null,
	Narration varchar2(200),
	WalletId Number not null,
	Amount Number,
	ModeNo char(1),
	ReferenceNo varchar2(20),
	SListId Number,
    FOREIGN KEY(WalletId) references Wallet(Id),
	FOREIGN KEY(PPId) references PP(Id)
);
Create table TransDocs(
	Id Number not null primary key,
    ExTransId Number not null,
	Name varchar2(50),
	Pic Blob,
    FOREIGN KEY(ExTransId) references ExtTrans(Id)
);
Create table Category(
	Id Number not null primary key,
	Name varchar2(30)
);
Create table ShoppingList(
	Id Number not null primary key,
	Dttm Date,
	NoOfSeconds int,
	Name varchar2(30),
	CatId Number not null,
	TransMade char(1),
	TemplateYN char(1),
    FOREIGN KEY(CatId) references Category(Id)
);
Create table ListItem(
	Id Number not null primary key,
	Name varchar2(30)
	);
Create table UOM(
	Id Number not null primary key,
	Name varchar2(30)
);
Create table SListDetail(
	SlNo Number,
	lstId Number not null,
	ItemId Number not null,
	UOMId Number not null,
	Qty Number,
	price Number,
	checkedYN char(1),
	Remark varchar2(30),
    FOREIGN KEY(lstId) references ShoppingList(Id),
	FOREIGN KEY(ItemId) references ListItem(Id),
	PRIMARY KEY(lstId,SlNo)
);
Create table ItemPrice(
	ItemId Number,
	UOMId Number,
	Price Number,
	Dt Date,
	FOREIGN KEY(ItemId) references ListItem(Id),
	FOREIGN KEY(UOMId) references UOM(Id),
	PRIMARY KEY(ItemId,UOMId)
);
Create table ServDef(
	Id Number not null primary key,
	Name varchar2(30),
	Ontime Date,
	isAM char(1),
	StrtDate Date,
	WalletId Number not null,
	AutoAddYN char(1),
	RgbVal int,
	OnNoOfSeconds int,
    FOREIGN KEY(WalletId) references Wallet(Id)
);
Create table ServDefDetail(
	Id Number not null primary key,
	ServDefId Number not null,
	ItemName varchar2(30),
	Price Number,
	DefaultQty Number,
	Frequency int,
    FOREIGN KEY(ServDefId) references ServDef(Id)
);
Create table RegServ(
	Id Number not null primary key,
	ServDefId Number not null,
	Dttm Date,
	Paid char(1),
	Ontime Date,
	NoOfSeconds int,
	AutoAddYN char(1),
	DeletedYN char(1),
    FOREIGN KEY(ServDefId) references ServDef(Id)
);
Create table RegServDetails(
     RegServId Number not null,
     SlNo Number,
     ServItemId Number not null,
     Price Number,
     Qty Number,
     FOREIGN KEy(RegServId) references RegServ(Id),
     FOREIGN KEY(ServItemId) references ServDefDetail(Id),
     PRIMARY KEY(RegServId,SlNo)
);
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
	Id Number not null primary key,
	dt Date unique,
	Income Number(9),
	Expense Number(9)
);

INSERT INTO PP(ID, NAME) VALUES ('1', 'Bank');
INSERT INTO PP(ID, NAME) VALUES ('2', 'Shopping');

INSERT INTO SYSTEMSETTINGS(OPENINGBANKBALANCE,OPENINGCASHBALANCE,REFLIQBALANCE,REFDIGITBALANCE,HOLIDAYUPDATIONDATE) VALUES(0,0,0,0,to_date('26/09/2019','dd/mm/yyyy'));

INSERT INTO USERSETTINGS(DC,LIQUIDWARNINGBALANCE,DIGITALWARNINGBALANCE,PERIODTYPE,FREQ1,FREQ2,WEEKDAY,PDINTERVALNUMBER,PDINTERVALTYPE,STARTHOMEWITH,LIQUIDWARNINGPERCENT,DIGITALWARNINGPERCENT,MIDMONTHDATE) VALUES('D',0,0,'D',2,-1,-1,1,'H','C',40,40,15);
INSERT INTO USERSETTINGS(DC,LIQUIDWARNINGBALANCE,DIGITALWARNINGBALANCE,PERIODTYPE,FREQ1,FREQ2,WEEKDAY,PDINTERVALNUMBER,PDINTERVALTYPE,STARTHOMEWITH,LIQUIDWARNINGPERCENT,DIGITALWARNINGPERCENT,MIDMONTHDATE) VALUES('C',0,0,'D',2,-1,-1,1,'H','C',40,40,15);
INSERT INTO USERSETTINGS(DC,LIQUIDWARNINGBALANCE,DIGITALWARNINGBALANCE,PERIODTYPE,FREQ1,FREQ2,WEEKDAY,PDINTERVALNUMBER,PDINTERVALTYPE,STARTHOMEWITH,LIQUIDWARNINGPERCENT,DIGITALWARNINGPERCENT,MIDMONTHDATE) VALUES('F',0,0,'D',2,-1,-1,1,'H','C',40,40,15);






