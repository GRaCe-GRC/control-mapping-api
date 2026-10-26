# control-mapping-api

Inner API for control mappings (Controls database).

> **Status:** CRUD for control mappings in the `control_mappings` schema of the `controls` database.

## Stack
- Java (latest LTS at setup), Spring Boot (latest stable at setup)
- Maven, via the Maven wrapper (`./mvnw`)
- PostgreSQL with Spring Data JDBC, schema managed by Flyway (`src/main/resources/db/migration`)

## Getting started
Start the database from [GRaCe-GRC-sandbox](https://github.com/GRaCe-GRC/GRaCe-GRC-sandbox), then the app:

```bash
(cd ../GRaCe-GRC-sandbox/compose && docker compose up -d --wait)
./mvnw spring-boot:run
```

On startup Flyway applies any new migrations to the `control_mappings` schema. Entities are Java
records mapped with Spring Data JDBC.

| Endpoint | Description |
|---|---|
| `GET http://localhost:8080/actuator/health` | Health status (`UP`) |
| `GET http://localhost:8080/actuator/info` | Application info |
| `GET http://localhost:8080/swagger-ui.html` | API documentation (OpenAPI at `/v3/api-docs`) |

## Control mappings API

A mapping links a requirement (regulatory-api) to a control (control-api). Both are stored as ids
only: there are no foreign keys across schemas, and this API does not check that they exist.

| Method and path | Body | Response |
|---|---|---|
| `POST /api/v1/control-mappings` | `requirementId`, `controlId`, `rationale` (optional), `createdBy` | `201`, empty body, `Location` header with the new mapping's URL |
| `GET /api/v1/control-mappings` | | `200` with the list of mappings; `?requirementId=<id>` (repeatable, for several requirements at once) and `?controlId=<id>` filter it, and can be combined |
| `GET /api/v1/control-mappings/{id}` | | `200` with the mapping |
| `PUT /api/v1/control-mappings/{id}` | `rationale` | `200` with the mapping (only `rationale` changes) |
| `DELETE /api/v1/control-mappings/{id}` | | `204` |

Errors use RFC 9457 problem details: `400` for invalid input (with an `errors` field per
property), `404` for an unknown id, `409` when the requirement is already mapped to the control.

```bash
curl -s -H 'Content-Type: application/json' \
  -d '{"requirementId":"<requirement id>","controlId":"<control id>","createdBy":"me"}' \
  http://localhost:8080/api/v1/control-mappings
curl -s 'http://localhost:8080/api/v1/control-mappings?requirementId=<id 1>&requirementId=<id 2>'
```

## Database

| Environment variable | Default |
|---|---|
| `CONTROL_MAPPING_DB_HOST` | `localhost` |
| `CONTROL_MAPPING_DB_PORT` | `5432` |
| `CONTROL_MAPPING_DB_NAME` | `controls` |
| `CONTROL_MAPPING_DB_USERNAME` | `control_mapping_api` |
| `CONTROL_MAPPING_DB_PASSWORD` | `control_mapping_api` |

The defaults match the sandbox and are for local use only. The app uses only the
`control_mappings` schema; `controls` in the same database belongs to control-api
([ADR 0006](https://github.com/GRaCe-GRC/GRaCe-GRC-docs/blob/main/adr/0006-controls-database-schemas.md)).

| Table | Migration |
|---|---|
| `control_mappings` | `V1__create_control_mappings.sql` |

## Test and build
```bash
./mvnw verify
```

Branch verify runs the same command on every push to a branch and on every pull request, through the
shared [Maven CI workflow](https://github.com/GRaCe-GRC/.github/blob/main/.github/workflows/maven-ci.yml) (`.github/workflows/branch-verify.yml`).

## Versions and images
Versions follow semantic versioning and come from the commit messages. Every merge to `main`
runs `.github/workflows/release.yml`: the CI again, then the shared
[release workflow](https://github.com/GRaCe-GRC/.github/blob/main/.github/workflows/README.md#release):

| Commit | Release |
|---|---|
| `fix: GRC-<n>: ...` | Patch |
| `feat: GRC-<n>: ...` | Minor |
| `chore:`, `docs:`, ... | No release |

A release creates the `vX.Y.Z` tag, a GitHub Release with notes, and the image
`ghcr.io/grace-grc/control-mapping-api:<version>` (also tagged `latest`). The version is not stored in
`pom.xml`: it uses `${revision}`, which is `0.0.1-SNAPSHOT` for local builds and the release
version in CI.

```bash
docker pull ghcr.io/grace-grc/control-mapping-api:latest
```

## Architecture
This repository is part of the **inner API layer**: services reach the system of records only through it.
Other projects use it through internal service APIs and events, referring to its entities by ID.
See the [GRaCe-GRC documentation hub](https://github.com/GRaCe-GRC/GRaCe-GRC-docs).

## Contributing
See the organization's [contributing guide](https://github.com/GRaCe-GRC/.github/blob/main/CONTRIBUTING.md).
Commit messages use Conventional Commits with the ticket ID, for example
`feat: GRC-12: add <change>`.

## License
Licensed under the [Apache License 2.0](LICENSE).
