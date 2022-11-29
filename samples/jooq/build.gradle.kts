import com.diffplug.gradle.spotless.SpotlessExtension
import io.github.meiblorn.requiredocker.RequreDockerReadinessProbe.Companion.logMessage
import nu.studer.gradle.jooq.JooqEdition
import nu.studer.gradle.jooq.JooqExtension
import nu.studer.gradle.jooq.JooqGenerate
import org.flywaydb.gradle.task.FlywayMigrateTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jooq.meta.jaxb.Logging

plugins {
    kotlin("jvm") version "1.7.21"
    id("nu.studer.jooq") version "8.0"
    id("org.flywaydb.flyway") version "9.8.1"
    id("io.github.meiblorn.require-docker") version "1.0.0"
}

repositories {
    mavenCentral()
}

dependencies {
    jooqGenerator("org.postgresql:postgresql")
}

configure<SourceSetContainer> {
    main {
        kotlin {
            // https://arturdryomov.dev/posts/kotlin-code-organization/
            srcDirs("src/main/kotlinExtensions")
        }
    }
    test {
        kotlin {
            // https://arturdryomov.dev/posts/kotlin-code-organization/
            srcDirs("src/test/kotlinExtensions")
        }
    }
}

configure<JavaPluginExtension> {
    toolchain {
        anguageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<JavaCompile> {
    with(options) {
        compilerArgs = listOf("-parameters")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        javaParameters = true
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val jooqFlywayMigrate by tasks.creating(FlywayMigrateTask::class) {
    url = "jdbc:postgresql://localhost:5432/postgres"
    user = "postgres"
    password = "postgres"
    schemas = arrayOf("public")
}

configure<JooqExtension> {
    version.set("3.17.5")
    edition.set(JooqEdition.OSS)

    configurations {
        val main by creating {
            generateSchemaSourceOnCompilation.set(true)
            with(jooqConfiguration) {
                logging = Logging.WARN
                with(jdbc) {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://localhost:5432/postgres"
                    user = "postgres"
                    password = "postgres"
                }
                with(generator) {
                    name = "org.jooq.codegen.KotlinGenerator"
                    with(database) {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                        excludes = "flyway_schema_history"
                    }
                    with(generate) {
                        isFluentSetters = true
                        isImmutableInterfaces = true
                        isImmutablePojos = true
                        isPojosAsKotlinDataClasses = true
                        isPojosEqualsAndHashCode = false
                        isPojosToString = false
                        isValidationAnnotations = true
                        isVarargSetters = true
                    }
                    with(target) {
                        packageName = "io.github.meiblorn.sample.generated.jooq"
                        directory = "src/main/kotlinGenerated"
                    }
                    with(strategy) {
                        name = "org.jooq.codegen.DefaultGeneratorStrategy"
                    }
                }
            }
        }
    }
}

requireDocker {
    spec("jooq") {
        container("postgres") {
            image("postgres:latest")
            portBindings("5432:5432")
            envVars(
                "POSTGRES_PASSWORD" to "postgres",
                "POSTGRES_USER" to "postgres",
                "POSTGRES_DB" to "postgres",
            )
            // readyCheck { logContains("database system is ready to accept connections") }
            readinessProbe(
                logMessage("""listening on IPv4 address "0.0.0.0", port 5432""")
            )
        }
    }
}

val generateJooq by tasks.getting(JooqGenerate::class)
requireDocker.spec("jooq") {
    requiredBy(jooqFlywayMigrate)
    requiredBy(generateJooq)
}
generateJooq.dependsOn(jooqFlywayMigrate)

sourceSets {
    main {
        java {
            srcDirs("src/main/kotlinGenerated")
        }
    }
}
