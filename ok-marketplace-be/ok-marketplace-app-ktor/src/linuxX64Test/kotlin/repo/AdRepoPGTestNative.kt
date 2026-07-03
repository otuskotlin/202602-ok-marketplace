package ru.otus.otuskotlin.marketplace.app.ktor.repo

import com.benasher44.uuid.uuid4
import kotlin.test.BeforeTest
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv
import ru.otus.otuskotlin.marketplace.app.ktor.MkplAppSettings
import ru.otus.otuskotlin.marketplace.common.MkplCorSettings
import ru.otus.otuskotlin.marketplace.common.models.MkplAd
import ru.otus.otuskotlin.marketplace.common.repo.IRepoAd
import ru.otus.otuskotlin.marketplace.repo.common.AdRepoInitialized
import ru.otus.otuskotlin.marketplace.repo.pgsqlx4k.RepoAdSql
import ru.otus.otuskotlin.marketplace.repo.pgsqlx4k.SqlProperties

/**
 * V2 PG тесты для linuxX64.
 *
 * Контейнер PostgreSQL поднимается Gradle-задачей при -PwithPg.
 * Порт из env (getenv), задаётся build.gradle.kts.
 *
 * V1 тесты на native не работают — V1 мапперы JVM-only.
 * Наследуем @Test методы от V2AdRepoBaseTest (commonTest).
 */
@OptIn(ExperimentalForeignApi::class)
open class V2AdRepoPGTestNative : V2AdRepoBaseTest() {

    override val workMode = ru.otus.otuskotlin.marketplace.api.v2.models.AdRequestDebugMode.TEST

    private val host = "localhost"
    private val user = "postgres"
    private val pass = "marketplace-pass"
    @OptIn(ExperimentalForeignApi::class)
    private val port = getenv("postgresPort")?.toKString()?.toInt() ?: 5432

    private fun repoUnderTestContainer(
        initObjects: List<MkplAd> = emptyList(),
        randomUuid: () -> String = { uuid4().toString() },
    ) = AdRepoInitialized(
        repo = RepoAdSql(
            SqlProperties(host = host, port = port, user = user, password = pass),
            randomUuid = randomUuid
        ),
        initObjects = initObjects,
    )

    override val appSettingsCreate: MkplAppSettings by lazy {
        mkAppSettings(repoUnderTestContainer(randomUuid = { uuidNew }))
    }
    override val appSettingsRead: MkplAppSettings by lazy {
        mkAppSettings(repoUnderTestContainer(initObjects = listOf(initAd), randomUuid = { uuidNew }))
    }
    override val appSettingsUpdate: MkplAppSettings by lazy {
        mkAppSettings(repoUnderTestContainer(initObjects = listOf(initAd), randomUuid = { uuidNew }))
    }
    override val appSettingsDelete: MkplAppSettings by lazy {
        mkAppSettings(repoUnderTestContainer(initObjects = listOf(initAd), randomUuid = { uuidNew }))
    }
    override val appSettingsSearch: MkplAppSettings by lazy {
        mkAppSettings(repoUnderTestContainer(initObjects = listOf(initAd), randomUuid = { uuidNew }))
    }
    override val appSettingsOffers: MkplAppSettings by lazy {
        mkAppSettings(repoUnderTestContainer(initObjects = listOf(initAd, initAdSupply), randomUuid = { uuidNew }))
    }

    private val cleanRepo = repoUnderTestContainer()

    @BeforeTest
    fun beforeTest() {
        val pgRepo = cleanRepo.repo as RepoAdSql
        pgRepo.clear()
    }

    private fun mkAppSettings(repo: IRepoAd) = MkplAppSettings(
        corSettings = MkplCorSettings(repoTest = repo, repoProd = repo)
    )

    companion object {
        protected val uuidNew = "10000000-0000-0000-0000-000000000001"
        protected val initAd = ru.otus.otuskotlin.marketplace.stubs.MkplAdStub.prepareResult {
            id = ru.otus.otuskotlin.marketplace.common.models.MkplAdId(uuidNew)
        }
        protected val initAdSupply = initAd.copy(adType = ru.otus.otuskotlin.marketplace.common.models.MkplDealSide.SUPPLY)
    }
}
