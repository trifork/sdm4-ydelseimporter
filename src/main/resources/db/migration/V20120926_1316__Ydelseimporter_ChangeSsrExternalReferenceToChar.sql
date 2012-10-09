-- fixes NSPSUPPORT-111
-- needs to run even on existing sdm3 schema
ALTER TABLE SSR MODIFY externalReference varchar(24) NOT NULL