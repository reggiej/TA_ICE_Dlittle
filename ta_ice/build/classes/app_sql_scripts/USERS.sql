--------------------------------------------------------
--  DDL for Table USERS
--------------------------------------------------------

  CREATE TABLE "THREAT_DB"."USERS" 
   (	"USER_SEQ" NUMBER, 
	"FIRSTNAME" VARCHAR2(256 BYTE), 
	"LASTNAME" VARCHAR2(256 BYTE), 
	"USER_MI_NAME" VARCHAR2(256 BYTE), 
	"USERNAME" VARCHAR2(7 BYTE), 
	"USER_TITLE" VARCHAR2(50 BYTE), 
	"USER_SECURE_PH" VARCHAR2(50 BYTE), 
	"USER_COMM_PH" VARCHAR2(50 BYTE), 
	"USER_CELL_PH" VARCHAR2(50 BYTE), 
	"PASSWORD" VARCHAR2(256 BYTE), 
	"ENABLED" NUMBER(*,0), 
	"EFFECTIVE_DT" TIMESTAMP (6), 
	"PASSWORD_CH_EFFECTIVE_TS" TIMESTAMP (6), 
	"CHANGE_PASSWORD" CHAR(1 CHAR), 
	"ORG_SEQ" NUMBER, 
	"SOCIAL_SECURITY_1" VARCHAR2(3 BYTE), 
	"SOCIAL_SECURITY_2" VARCHAR2(2 BYTE), 
	"SOCIAL_SECURITY_3" VARCHAR2(4 BYTE)
   ) SEGMENT CREATION IMMEDIATE 
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS" ;
