CREATE TABLE IF NOT EXISTS LPR (
  pk bigint NOT NULL auto_increment,

  -- cpr numre er base64 af hashede numre
  patientCpr varchar(80) NOT NULL,

  admittedStart datetime NOT NULL,
  admittedEnd datetime,

  lprReference char(60) NOT NULL,

  relationType varchar(40) NOT NULL,
  organisationIdentifier varchar(7), -- ydernummer or sks

  PRIMARY KEY (pk),
  INDEX LPR_INDEX USING HASH (patientCpr, organisationIdentifier),
  INDEX LPR_REF_INDEX USING HASH (lprReference)
) ENGINE=InnoDB COLLATE=utf8_bin;
