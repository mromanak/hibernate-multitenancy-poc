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
VALUES ('arquebus', 'Arquebus', 'arquebus', 'http://localhost:9090/realms/arquebus'),
       ('balam', 'Balam', 'balam', 'http://localhost:9090/realms/balam');

CREATE SCHEMA "arquebus";
CREATE SCHEMA "balam";
