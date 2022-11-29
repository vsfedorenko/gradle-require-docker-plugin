Gradle RequireDocker plugin
===

This plugin allows you to require Docker containers to be running before executing a Gradle task.

For example, you can **require a Postgres container to be running before** executing
a **Flyway migration and a JOOQ code generation** or **integration tests** tasks. Plugin also
maintains containers lifecycle, so you **don't need to worry about stopping containers after**.

```kotlin
plugins {
    id("io.github.meiblorn.require-docker") version "1.0.0"
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

- [ ] Publish plugin to Gradle Plugin Portal
- [ ] Add test coverage
- [ ] Add GitHub Actions workflow
- [ ] Add badges to README.md
- [ ] Add more samples
- [ ] Clean up code
    - [ ] Make tasks more specific by utilising specs instances
      instead of direct configuration in TaskFactory
    - [ ] Move extensions to separate source set
- [ ] Cover with unit tests
- [ ] Cover with integration tests
- [ ] Add java docs
- [ ] Add docs (preferably using [docusaurus](https://docusaurus.io/))
