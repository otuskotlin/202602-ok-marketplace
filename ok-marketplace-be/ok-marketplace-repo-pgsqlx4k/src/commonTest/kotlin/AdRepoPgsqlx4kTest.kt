package ru.otus.otuskotlin.marketplace.repo.pgsqlx4k

import com.benasher44.uuid.uuid4
import ru.otus.otuskotlin.marketplace.backend.repo.tests.*
import ru.otus.otuskotlin.marketplace.common.models.MkplAd
import ru.otus.otuskotlin.marketplace.libs.sysenv.sysEnv
import ru.otus.otuskotlin.marketplace.repo.common.AdRepoInitialized

/**
 * Создаёт RepoAdSql с портом из окружения.
 * Платформозависимая реализация — JVM читает System.getProperty, native — getenv.
 */
fun testRepo(
    initObjects: List<MkplAd>,
    randomUuid: () -> String = { uuid4().toString() },
): AdRepoInitialized {
    val port = sysEnv("postgresPort")?.toInt() ?: 5432
    val repo = RepoAdSql(
        SqlProperties(
            host = "localhost",
            port = port,
            user = "postgres",
            password = "marketplace-pass",
        ),
        randomUuid = randomUuid,
    )
    repo.clear()
    return AdRepoInitialized(repo, initObjects)
}

class AdRepoPgsqlx4kCreateTest : RepoAdCreateTest() {
    override val repo = testRepo(initObjects, randomUuid = { uuidNew.asString() })
}

class AdRepoPgsqlx4kDeleteTest : RepoAdDeleteTest() {
    override val repo = testRepo(initObjects, randomUuid = { uuid4().toString() })
}

class AdRepoPgsqlx4kReadTest : RepoAdReadTest() {
    override val repo = testRepo(initObjects, randomUuid = { uuid4().toString() })
}

class AdRepoPgsqlx4kSearchTest : RepoAdSearchTest() {
    override val repo = testRepo(initObjects, randomUuid = { uuid4().toString() })
}

class AdRepoPgsqlx4kUpdateTest : RepoAdUpdateTest() {
    override val repo = testRepo(initObjects, randomUuid = { lockNew.asString() })
}
