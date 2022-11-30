Gradle RequireDocker plugin
===

[![Build Status](https://github.com/meiblorn/gradle-require-docker-plugin/actions/workflows/build-on-push-to-main.yml/badge.svg?branch=main)](https://github.com/meiblorn/gradle-require-docker-plugin/actions/workflows/build-on-push-to-main.yml?query=branch%3Amain)
[![codecov](https://codecov.io/gh/meiblorn/gradle-require-docker-plugin/branch/main/graph/badge.svg?token=7SWSOTIBMX)](https://codecov.io/gh/meiblorn/gradle-require-docker-plugin)
[![GitHub Release](https://img.shields.io/github/release/meiblorn/gradle-require-docker-plugin.svg?label=GitHub%20Release)](https://github.com/meiblorn/gradle-require-docker-plugin/releases)
[![Gradle Plugins Release](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/io/github/meiblorn/require-docker/io.github.meiblorn.require-docker.gradle.plugin/maven-metadata.xml.svg?label=Gradle%20Plugin%20Portal)](https://plugins.gradle.org/plugin/io.github.meiblorn.require-docker)
[![license](https://img.shields.io/github/license/meiblorn/gradle-require-docker-plugin.svg)](LICENSE)
[![Semantic Release](https://img.shields.io/badge/%20%20%F0%9F%93%A6%F0%9F%9A%80-semantic--release-e10079.svg)](https://github.com/semantic-release/semantic-release)

> I am looking for help to develop this plugin. 
> I have a lot of ideas, but I don't have enough time to implement them all.
> If you are interested, feel free to open 
> an [issue](https://github.com/meiblorn/gradle-require-docker-plugin/issues) 
> or a [discussion](https://github.com/meiblorn/gradle-require-docker-plugin/discussions) 
> to talk about it.
> Also, I will be happy to add you to the list of contributors.
> Thank you!

Integrate docker containers into your workflow.
**Require** Docker containers to be **running before** executing a Gradle task and **shutdown after**.

With this plugin, you can **require a Postgres container to be running before** executing
a **Flyway migration and a JOOQ code generation** or **integration tests** tasks. Plugin also
maintains containers lifecycle, so you **don't need to worry about stopping and removing containers after**.

```kotlin
plugins {
    id("io.github.meiblorn.require-docker") version "x.y.z"
}

requireDocker {
    val jooq by requireDocker.specs.creating {
        val postgres by contaienrs.creating {
            image("postgres:latest")
            portBindings("5432:5432")
            envVars(
                "POSTGRES_PASSWORD" to "postgres",
                "POSTGRES_USER" to "postgres",
                "POSTGRES_DB" to "postgres",
            )
        }
    }
}

// Create Flyway migrations task
// This task creates schema and tables in the database
val jooqFlywayMigrate by tasks.creating {
    url = "jdbc:postgresql://localhost:5432/postgres"
    user = "postgres"
    password = "postgres"
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
    - [x] Move Kotlin extensions to separate Gradle source set
    - [ ] Build internal task graph and then process it in a single place
          instead of iterating specs and containers in nested loops
    - [ ] Utilise dependency injection features of Gradle to simplify code
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

Inspired by [Avast's Docker Compose  plugin](https://github.com/avast/gradle-docker-compose-plugin) and
[Monosoul's JOOQ plugin](https://github.com/monosoul/jooq-gradle-plugin).
