DROP TABLE IF EXISTS public.tenant CASCADE;
CREATE TABLE public.tenant
(
    id     VARCHAR(255) NOT NULL,
    name   VARCHAR(255) NOT NULL,
    schema VARCHAR(255) NOT NULL,
    issuer VARCHAR(255) NOT NULL,
    CONSTRAINT pk_tenant PRIMARY KEY (id)
);

ALTER TABLE public.tenant
    ADD CONSTRAINT uc_tenant_issuer UNIQUE (issuer);

ALTER TABLE public.tenant
    ADD CONSTRAINT uc_tenant_name UNIQUE (name);

ALTER TABLE public.tenant
    ADD CONSTRAINT uc_tenant_schema UNIQUE (schema);

INSERT INTO public.tenant
VALUES ('arquebus', 'Arquebus', 'arquebus', 'https://idp.example.org/arquebus'),
       ('balam', 'Balam', 'balam', 'https://idp.example.org/balam');

DROP SCHEMA IF EXISTS "arquebus" CASCADE;
CREATE SCHEMA "arquebus";

DROP SCHEMA IF EXISTS "balam" CASCADE;
CREATE SCHEMA "balam";
