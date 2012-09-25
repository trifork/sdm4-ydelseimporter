CREATE TABLE SSR (
  pk BIGINT(15) AUTO_INCREMENT NOT NULL PRIMARY KEY,

    -- cpr numre er base64 af hashede numre
  patientCpr varchar(80) NOT NULL,

  doctorOrganisationIdentifier varchar(6) NOT NULL, -- ydernummer

  admittedStart datetime NOT NULL,
  admittedEnd datetime NOT NULL,

  externalReference varchar(24) NOT NULL
) ENGINE=InnoDB COLLATE=utf8_bin;

CREATE INDEX SSR_INDEX USING HASH on SSR(patientCpr, doctorOrganisationIdentifier);
