# Hibernate Multitenancy Proof-Of-Concept

## Overview

I was bored and wanted to see how easily I could implement a REST application that supported multitenancy, so I made
this. This sample app builds upon
[multitenant-oauth2-spring-boot-starter](https://github.com/quantics-io/multitenant-oauth2-spring-boot-starter/tree/main)
which is itself sample app demonstrating the use of the
[Spring Boot starter library from Quantics](https://github.com/quantics-io/multitenant-oauth2-spring-boot-starter).

Relative the project it's based on, this project:

- Implements:
    - An entity model with more and more complicated fields.
    - A more complete controller with role-based access controls on its endpoints.
    - OpenAPI documentation.
- Adds:
    - Keycloak exports that can be used to create sample realms for OIDC authentication.
    - Flyway migration scripts to initialize the database.
    - Javadocs that attempt to explain (my understanding of) how each class figures into the Spring magic that makes
      this app work.
    - A lot of my own weird personal preferences (sorry.)
- Removes:
    - The ability to use header-based authentication (because header-based auth did not provide the roles that we need
      for role-based access controls.)

## Running the Application

### Requirements

To run this application, you will need:

- `java` version 17 or higher
- `docker-compose`

### Starting the Docker Dependencies

This command will use `docker-compose` to stand up the three services that the application depends on:

```shell
docker-compose -f docker/docker-compose.yml --env-file docker/.env up
```

The services started are:

| Name             | Service                                          | Port | Data Directory                |
|------------------|--------------------------------------------------|------|-------------------------------|
| `keycloak`       | Keycloak instance used for user authentication   | 9090 |                               |
| `keycloak-db`    | Postgres database used to store Keycloak data    | 9432 | `docker/keycloak-db-data/`    |
| `multitenant-db` | Postgres database used to store application data | 5432 | `docker/multitenant-db-data/` |

### Initializing Keycloak

The first time Keycloak is started, you will need to create realms for the tenants. To do so:

1. Log into the [Keycloak Admin Console](http://localhost:9090/admin/) using the username `admin` and `admin-pw`
2. From the Admin Console, on the left sidebar, click the top drop-down menu, and then click Create Realm
3. On the Create Realm page, upload the file [keycloak/arquebus-realm.json](keycloak/arquebus-realm.json) and then click
   Create to create the Arquebus realm
4. Repeat step 2 to get back to the Create Realm page
5. On the Create Realm page, upload the file [keycloak/balam-realm.json](keycloak/balam-realm.json) and then click
   Create to create the Balam realm

### Initializing the Application Database

No explicit action is needed to manually initialize the database tables. The application
uses [Flyway](https://flywaydb.org/) to manage the structure of the database. When the application starts up, it will
apply the SQL migration scripts in [src/main/resources/db/migration](src/main/resources/db/migration) to initialize the
database, and then create Flyway metadata tables to track which migration scripts have already been applied.

### Running the Application

This command will use the checked-in Maven wrapper to start the application on port 8080:

```shell
./mvnw spring-boot:run
```

### Authentication

The Keycloak exports created in the Initializing Keycloak section define two tenants (Arquebus and Balam) and two users
for each tenant (a `-user` user that can only read existing Assets, and an `-admin` user that can read and write
Assets.) Users only have access to the records associated with their tenant. Arquebus users cannot access Balam records
and vice-versa.

| `TENANT`   | `USERNAME`       | `PASSWORD`          | `CLIENT_SECRET`                    | `ROLES`                        |
|------------|------------------|---------------------|------------------------------------|--------------------------------|
| `arquebus` | `arquebus-user`  | `arquebus-user-pw`  | `yFH7OxE4Th56Qosi7J0hl9CEbA0K3Fk4` | `read:asset`                   |
| `arquebus` | `arquebus-admin` | `arquebus-admin-pw` | `yFH7OxE4Th56Qosi7J0hl9CEbA0K3Fk4` | `read:asset`<br/>`write:asset` |
| `balam`    | `balam-user`     | `balam-user-pw`     | `Zqq7CmbKg1sIKnZLfJLqhYzwJz71uhDW` | `read:asset`                   |
| `balam`    | `balam-admin`    | `balam-admin-pw`    | `Zqq7CmbKg1sIKnZLfJLqhYzwJz71uhDW` | `read:asset`<br/>`write:asset` |

To obtain an auth token for a user token, make the following request to Keycloak and extract the `access_token` field
from the response:

```shell
curl --silent --location 'http://localhost:9090/realms/${TENANT}/protocol/openid-connect/token' \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'client_id=example-application' \
  --data-urlencode 'grant_type=password' \
  --data-urlencode 'client_secret=${CLIENT_SECRET}' \
  --data-urlencode 'username=${USERNAME}' \
  --data-urlencode 'password=${PASSWORD}' \
  --data-urlencode 'scope=roles'
```

When making a request to the application, authenticate by sending an `Authorization` header with the
value `Bearer ${ACCESS_TOKEN}`. Auth tokens are valid for 5 minutes after creation, and the `refresh_token` included in
the auth token response can be used to refresh the token for 30 minutes after token creation.

### Available Endpoints

The application creates a Swagger UI that documents the available endpoints. With the application running, it can be
accessed at [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html).

## Features of Interest

If you are unfamiliar with Spring, this project shows off some features I really like about it, including:

- No/low-code repository classes. [Spring Data](https://spring.io/projects/spring-data) does some pretty serious magic
  to automatically generate Object-Relational Mapping code:
    - [AssetRepository](src/main/java/com/mromanak/multitenant/test/repository/AssetRepository.java) supports basic CRUD
      operations and pagination for Assets simply by extending an interface.
    - [TenantRepository](src/main/java/com/mromanak/multitenant/test/repository/TenantRepository.java) additionally
      shows how Spring Data can automatically generate more complex ORM methods by extending an interface and following
      a method naming convention.
- End-to-end integration
  tests. [AssetControllerIT](src/test/java/com/mromanak/multitenant/test/controller/AssetControllerIT.java) stands up
  and populates an in-memory test database, and allows us to simulate HTTP requests end-to-end. This allows us to verify
  that configuration, controllers, services, and repositories work together as a full stack with minimal mocking.
- Database change management via [Flyway](https://flywaydb.org/). This allows us to check versioned database migration
  scripts into the project and allows the application to ensure that the structure of the database reflects what the
  application needs to function. [FlywayConfig](src/main/java/com/mromanak/multitenant/test/config/FlywayConfig.java)
  also takes care of both the shared database schema that all tenants use and the database schema individual to each
  tenant.
- Spring is smart enough to generate OpenAPI documentation that incorporates constraint validation annotations. For
  example, when using the `@Pattern` annotation on a request parameter, the OpenAPI documentation will document the
  parameter's `pattern` property without needing to be explicitly told to do so by the `@Schema` annotation.

## Future Improvements

In the future, I would like to extend this project to:

- Implement OIDC authentication correctly. I am inexperienced with Keycloak and OIDC, but I don't think users are
  supposed to require access to a client secret to authenticate. I suspect the user is meant to pass their credentials
  to the client and then the client is meant to pass both the credentials and the client secret to Keycloak, but I need
  to do more research on that.
- Work with an entity that contains a geometry/geography column.
- Make more extensive use of Spring Data.
    - Demonstrate how more advanced queries would be constructed.
    - Demonstrate how relations between entities would be modeled.
- Use Flyway in our integration tests.
    - Start each test by creating a freshly-migrated database and then inserting sample data.
    - Automatically incorporate the latest migration scripts.

## Known Issues

### Project Is Stuck on Spring Boot V2

The [Spring Boot starter library from Quantics](https://github.com/quantics-io/multitenant-oauth2-spring-boot-starter)
that powers the multitenancy capabilities of this application currently does not work with Spring Boot major version 3.

The most obvious issue is that major version 2 used the JavaEE framework whereas major version 3 uses the JakartaEE
framework. Those two frameworks are effectively the same, except that the authors are no longer allowed to use
the Java branding and thus had to migrate package namespaces from `javax` to `jakarta`. That migration breaks a lot of
import statements. By itself, changing the imports would be an easy problem to fix, but I do not have the expertise to
say those are the only breaking changes, and for my own sanity I'm not going to set out to fix that library on my own.

### Project Is Not Production-Ready

Just to state it explicitly, the security configuration included in this project is completely insufficient for a
production environment. It is only suitable for demonstration and testing purposes.

- The Docker Keycloak service runs in dev mode and directly warns users not to use it in a production environment.
- Postgres and Keycloak credentials are included in plaintext this repository, and should be considered to be
  compromised.
- As noted in the Future Improvements section, I probably implemented OIDC wrong.
