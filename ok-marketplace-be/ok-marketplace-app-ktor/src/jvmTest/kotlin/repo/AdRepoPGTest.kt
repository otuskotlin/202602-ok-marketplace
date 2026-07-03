package ru.otus.otuskotlin.marketplace.app.ktor.repo

import com.benasher44.uuid.uuid4
import ru.otus.otuskotlin.marketplace.common.models.MkplAd
import ru.otus.otuskotlin.marketplace.repo.common.AdRepoInitialized
import ru.otus.otuskotlin.marketplace.repo.pgsqlx4k.RepoAdSql
import ru.otus.otuskotlin.marketplace.repo.pgsqlx4k.SqlProperties

/**
 * Вспомогательный объект для PG-тестов.
 *
 * Контейнер PostgreSQL поднимается Gradle-задачей jvmTestPg.
 * Порт передаётся в тест через system property "postgresPort".
 */
object AdRepoPGTest {

    private const val HOST = "localhost"
    private const val USER = "postgres"
    private const val PASS = "marketplace-pass"
    private val PORT = System.getProperty("postgresPort")?.toInt() ?: 5432

    fun repoUnderTestContainer(
        initObjects: Collection<MkplAd> = emptyList(),
        randomUuid: () -> String = { uuid4().toString() },
    ) = AdRepoInitialized(
        repo = RepoAdSql(
            SqlProperties(
                host = HOST,
                user = USER,
                password = PASS,
                port = PORT,
            ),
            randomUuid = randomUuid
        ),
        initObjects = initObjects,
    )
}
