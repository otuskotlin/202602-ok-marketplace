package ru.otus.otuskotlin.marketplace.e2e.be.docker

import ru.otus.otuskotlin.marketplace.e2e.be.base.AbstractDockerCompose

object KafkaDockerCompose : AbstractDockerCompose(
    "kafka", 9092, "docker-compose-kafka.yml"
)
