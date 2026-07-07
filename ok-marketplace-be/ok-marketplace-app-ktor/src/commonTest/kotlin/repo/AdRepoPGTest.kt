package ru.otus.otuskotlin.marketplace.app.ktor.repo

import com.benasher44.uuid.uuid4
import ru.otus.otuskotlin.marketplace.common.models.MkplAd
import ru.otus.otuskotlin.marketplace.libs.sysenv.sysEnv
import ru.otus.otuskotlin.marketplace.repo.common.AdRepoInitialized
import ru.otus.otuskotlin.marketplace.repo.pgsqlx4k.RepoAdSql
import ru.otus.otuskotlin.marketplace.repo.pgsqlx4k.SqlProperties

/**
 * Вспомогательный объект для PG-тестов.
 * Порт читается через sysEnv — единый API для JVM и native.
 * Контейнер PostgreSQL управляется Gradle-задачами pgUp/pgDn.
 */
object AdRepoPGTest {

    private val host get() = sysEnv("postgresHost") ?: "localhost"
    private val port get() = sysEnv("postgresPort")?.toInt() ?: 5432
    private val user get() = sysEnv("postgresUser") ?: "postgres"
    private val pass get() = sysEnv("postgresPass") ?: "marketplace-pass"

    fun repoUnderTestContainer(
        initObjects: Collection<MkplAd> = emptyList(),
        randomUuid: () -> String = { uuid4().toString() },
    ) = AdRepoInitialized(
        repo = RepoAdSql(
            SqlProperties(
                host = host,
                port = port,
                user = user,
                password = pass,
            ),
            randomUuid = randomUuid,
        ),
        initObjects = initObjects,
    )
}
