import org.testcontainers.containers.ComposeContainer
import ru.otus.otuskotlin.marketplace.plugin.DockerBuildTask

plugins {
    id("build-docker")
}

docker {
    images.register("Cs") {
        buildContext = project.layout.buildDirectory.dir("docker").get().toString()
        imageName = project.name
        imageTag = "${project.version}"
    }
}

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        // Testcontainers core + Docker Compose модуль
        // classpath("org.testcontainers:testcontainers:2.**")
        classpath(libs.testcontainers.core)
    }
}

val csContainer: ComposeContainer by lazy {
    ComposeContainer(
        file("src/test/compose/docker-compose-cs.yml")
    )
        .withExposedService("cassandra", 9042)
}

tasks {
    val buildImages by registering {
        dependsOn("dockerBuildCs")
    }

    val cassandraDn by registering {
        group = "db"
        doFirst {
            println("Stopping Cassandra...")
            csContainer.stop()
            println("Cassandra stopped")
        }
    }
    val cassandraUp by registering {
        group = "db"
        doFirst {
            println("Starting Cassandra...")
            csContainer.start()
            println("Cassandra started at port: ${csContainer.getServicePort("cassandra", 9042)}")
        }
        finalizedBy(cassandraDn)
    }

}

afterEvaluate {
    tasks {
        named("dockerBuildCs", DockerBuildTask::class) {
            doFirst {
                copy {
                    from("src/main/liquibase") //{ into("${buildContext.get()}/liquibase") }
                    from("src/main/docker/Dockerfile")
                    into(buildContext)
                }
            }
        }
    }
}