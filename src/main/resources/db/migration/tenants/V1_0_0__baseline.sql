CREATE TABLE asset
(
    id                UUID                     NOT NULL,
    name              VARCHAR(50)              NOT NULL,
    created_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    server_url        VARCHAR(255),
    CONSTRAINT pk_asset PRIMARY KEY (id)
);