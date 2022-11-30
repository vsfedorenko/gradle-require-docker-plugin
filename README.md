Gradle RequireDocker plugin
===

[![Build Status](https://github.com/meiblorn/gradle-require-docker-plugin/actions/workflows/build-on-push-to-main.yml/badge.svg?branch=main)](https://github.com/meiblorn/gradle-require-docker-plugin/actions/workflows/build-on-push-to-main.yml?query=branch%3Amain)
[![codecov](https://codecov.io/gh/meiblorn/gradle-require-docker-plugin/branch/main/graph/badge.svg?token=7SWSOTIBMX)](https://codecov.io/gh/meiblorn/gradle-require-docker-plugin)
[![GitHub Release](https://img.shields.io/github/release/meiblorn/gradle-require-docker-plugin.svg?label=GitHub%20Release)](https://github.com/meiblorn/gradle-require-docker-plugin/releases)
[![Gradle Plugins Release](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/io/github/meiblorn/require-docker/io.github.meiblorn.require-docker.gradle.plugin/maven-metadata.xml.svg?label=Gradle%20Plugin%20Portal)](https://plugins.gradle.org/plugin/io.github.meiblorn.require-docker)
[![license](https://img.shields.io/github/license/meiblorn/gradle-require-docker-plugin.svg)](LICENSE)
[![Semantic Release](https://img.shields.io/badge/%20%20%F0%9F%93%A6%F0%9F%9A%80-semantic--release-e10079.svg)](https://github.com/semantic-release/semantic-release)

This plugin allows you to require Docker containers to be running before executing a Gradle task.

For example, you can **require a Postgres container to be running before** executing
a **Flyway migration and a JOOQ code generation** or **integration tests** tasks. Plugin also
maintains containers lifecycle, so you **don't need to worry about stopping and removing containers after**.

```kotlin
plugins {
    id("io.github.meiblorn.require-docker") version "x.y.z"
}

requireDocker {
    docker {
        // OPTIONAL: Docker client configuration
        // Usually, you will never need to configure it
    }
    
    // You can create multiple specs
    spec("main") {
        // ...
    }
    
    // Sample spec for JOOQ code generation
    spec("jooq") {
        container("postgres") {
            image("postgres:latest")
            portBindings("5432:5432")
            envVars(
                "POSTGRES_PASSWORD" to "postgres",
                "POSTGRES_USER" to "postgres",
                "POSTGRES_DB" to "postgres",
            )
        }
        
        // You can declare multiple containers
        container("other") {
            // ...
        }
    }
    
    // Another, third spec
    spec("other") {
        // ...
    }
}

// Create Flyway migrations task
// This task creates schema and tables in the database
val jooqFlywayMigrate by tasks.creating {
    with(requireDocker.spec("jooq").container("postgres")) {
        url = "jdbc:postgresql://localhost:${portBindings[5432]}/${envVars["POSTGRES_DB"]}"
        user = envVars["POSTGRES_USER"]
        password = envVars["POSTGRES_PASSWORD"]
    }
}

val generateJooq by tasks.getting {
    // Declare a dependency on the jooqFlywayMigrate task
    // NOTE: JOOQ requires the database schema 
    //       to be created before generating the code
    dependsOn(jooqFlywayMigrate)
}

// Enforce containers to be running before 
// executing jooqFlywayMigrate and generateJooq tasks
requireDocker.spec("jooq") {
    requiredBy(jooqFlywayMigrate)
    requiredBy(generateJooq)
}
```

## TODO:

- [x] Publish plugin to Gradle Plugin Portal
- [x] Add test coverage
- [x] Add GitHub Actions workflow
- [x] Add badges to README.md
- [ ] Add more samples
- [ ] Clean up code
    - [ ] Make tasks more specific by utilising specs instances
      instead of direct configuration in TaskFactory
    - [ ] Move extensions to separate source set
- [ ] Cover with unit tests
- [ ] Cover with integration tests
  - [ ] Check Docker client
  - [ ] Check Bmuschko's Gradle plugin conflicts
  - [ ] Check Spring Boot Gradle plugin conflicts
  - [ ] Check Micronaut Gradle plugin conflicts
- [ ] Add java docs
- [ ] Add docs (preferably using [docusaurus](https://docusaurus.io/))

## License

RequireDocker plugin is licensed under the **MIT** License. See [LICENSE](LICENSE) for more information.

Project heavily relies on [Bmuschko's Docker plugin](https://github.com/bmuschko/gradle-docker-plugin).

Inspired by [Abast's Docker Compose  plugin](https://github.com/avast/gradle-docker-compose-plugin) and
[Monosoul's JOOQ plugin](https://github.com/monosoul/jooq-gradle-plugin).
