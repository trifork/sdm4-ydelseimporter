CREATE TABLE IF NOT EXISTS SSR (
  pk BIGINT(15) AUTO_INCREMENT NOT NULL PRIMARY KEY,

    -- cpr numre er base64 af hashede numre
  patientCpr varchar(80) NOT NULL,

  doctorOrganisationIdentifier varchar(6) NOT NULL, -- ydernummer

  admittedStart datetime NOT NULL,
  admittedEnd datetime NOT NULL,

  externalReference char(24) NOT NULL,
  INDEX SSR_INDEX USING HASH (patientCpr, doctorOrganisationIdentifier)
) ENGINE=InnoDB COLLATE=utf8_bin;
