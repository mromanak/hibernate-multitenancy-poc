DROP TABLE IF EXISTS arquebus.asset CASCADE;
CREATE TABLE arquebus.asset
(
    id                UUID                     NOT NULL,
    name              VARCHAR(50)              NOT NULL,
    created_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    server_url        VARCHAR(255),
    CONSTRAINT pk_asset PRIMARY KEY (id)
);

INSERT INTO arquebus.asset (id, name, created_timestamp, server_url)
VALUES ('7471da05-d4ba-4531-ab64-755b94c88635', 'V.IV Rusty', '1970-01-01T00:00:00.000Z', 'https://arquebus.space/steel-haze');

DROP TABLE IF EXISTS balam.asset CASCADE;
CREATE TABLE balam.asset
(
    id                UUID                     NOT NULL,
    name              VARCHAR(50)              NOT NULL,
    created_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    server_url        VARCHAR(255),
    CONSTRAINT pk_asset PRIMARY KEY (id)
);

INSERT INTO balam.asset (id, name, created_timestamp, server_url)
VALUES ('b74df32a-99c9-482c-87ca-eccc7013197f', 'G5 Iguazu', '1970-01-01T00:00:00.000Z', 'https://balam.space/head-bringer');