package ru.otus.otuskotlin.marketplace.app.ktor.repo

import kotlin.test.BeforeTest
import ru.otus.otuskotlin.marketplace.api.v2.models.AdRequestDebugMode
import ru.otus.otuskotlin.marketplace.app.ktor.MkplAppSettings
import ru.otus.otuskotlin.marketplace.common.MkplCorSettings
import ru.otus.otuskotlin.marketplace.common.repo.IRepoAd
import ru.otus.otuskotlin.marketplace.repo.pgsqlx4k.RepoAdSql

open class V2AdRepoPGTest : V2AdRepoBaseTest() {
    override val workMode = AdRequestDebugMode.PROD

    private fun mkAppSettings(repo: IRepoAd) = MkplAppSettings(
        corSettings = MkplCorSettings(
            repoTest = repo,
            repoProd = repo,
        )
    )

    override val appSettingsCreate: MkplAppSettings by lazy {
        mkAppSettings(repo = AdRepoPGTest.repoUnderTestContainer(randomUuid = { uuidNew }))
    }
    override val appSettingsRead: MkplAppSettings by lazy {
        mkAppSettings(repo = AdRepoPGTest.repoUnderTestContainer(initObjects = listOf(initAd), randomUuid = { uuidNew }))
    }
    override val appSettingsUpdate: MkplAppSettings by lazy {
        mkAppSettings(repo = AdRepoPGTest.repoUnderTestContainer(initObjects = listOf(initAd), randomUuid = { uuidNew }))
    }
    override val appSettingsDelete: MkplAppSettings by lazy {
        mkAppSettings(repo = AdRepoPGTest.repoUnderTestContainer(initObjects = listOf(initAd), randomUuid = { uuidNew }))
    }
    override val appSettingsSearch: MkplAppSettings by lazy {
        mkAppSettings(repo = AdRepoPGTest.repoUnderTestContainer(initObjects = listOf(initAd), randomUuid = { uuidNew }))
    }
    override val appSettingsOffers: MkplAppSettings by lazy {
        mkAppSettings(repo = AdRepoPGTest.repoUnderTestContainer(initObjects = listOf(initAd, initAdSupply), randomUuid = { uuidNew }))
    }

    private val cleanRepo = AdRepoPGTest.repoUnderTestContainer()

    @BeforeTest
    fun beforeTest() {
        val pgRepo = cleanRepo.repo as RepoAdSql
        pgRepo.clear()
    }
}
