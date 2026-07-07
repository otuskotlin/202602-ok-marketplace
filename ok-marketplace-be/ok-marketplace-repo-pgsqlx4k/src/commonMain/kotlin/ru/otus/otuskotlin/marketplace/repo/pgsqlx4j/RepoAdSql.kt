package ru.otus.otuskotlin.marketplace.repo.pgsqlx4k

import com.benasher44.uuid.uuid4
import io.github.smyrgeorge.sqlx4k.ConnectionPool
import io.github.smyrgeorge.sqlx4k.Statement
import io.github.smyrgeorge.sqlx4k.postgres.postgreSQL
import kotlinx.coroutines.runBlocking
import ru.otus.otuskotlin.marketplace.common.helpers.errorSystem
import ru.otus.otuskotlin.marketplace.common.models.*
import ru.otus.otuskotlin.marketplace.common.repo.*
import ru.otus.otuskotlin.marketplace.common.repo.exceptions.RepoEmptyLockException
import ru.otus.otuskotlin.marketplace.repo.common.IRepoAdInitializable

class RepoAdSql(
    properties: SqlProperties = SqlProperties(),
    private val randomUuid: () -> String = { uuid4().toString() },
) : IRepoAd, IRepoAdInitializable {

    private val db by lazy {
        postgreSQL(
            url = properties.url,
            username = properties.user,
            password = properties.password,
            options = ConnectionPool.Options(maxConnections = properties.maxConnections),
        )
    }

    private val dbName = "\"${properties.schema}\".\"${properties.table}\""
    private val cols = SqlFields.allFields.joinToString { it.quoted() }

    override suspend fun createAd(rq: DbAdRequest): IDbAdResponse = tryAdMethod {
        val ad = rq.ad.copy(id = MkplAdId(randomUuid()), lock = MkplAdLock(randomUuid()))
        val stmt = Statement.create(SqlQueryBuilder.insert(dbName, cols)).bindAd(ad)
        val rows: List<MkplAd> = db.fetchAll(stmt, MkplAdRowMapper).getOrThrow()
        if (rows.isEmpty()) throw RuntimeException("DB error: insert returned no rows")
        DbAdResponseOk(rows.first())
    }

    override suspend fun readAd(rq: DbAdIdRequest): IDbAdResponse = tryAdMethod {
        val stmt = Statement.create(SqlQueryBuilder.read(dbName, cols))
            .bind(SqlFields.ID, rq.id.asString())
        val rows: List<MkplAd> = db.fetchAll(stmt, MkplAdRowMapper).getOrThrow()
        if (rows.isEmpty()) errorNotFound(rq.id)
        else DbAdResponseOk(rows.first())
    }

    override suspend fun updateAd(rq: DbAdRequest): IDbAdResponse = tryAdMethod {
        val newAd = rq.ad.copy(lock = MkplAdLock(randomUuid()))
        val stmt = Statement.create(SqlQueryBuilder.update(dbName, cols))
            .bindAd(newAd)
            .bind(SqlFields.LOCK_OLD, rq.ad.lock.asString())
        val rows: List<MkplAd> = db.fetchAll(stmt, MkplAdRowMapper).getOrThrow()
        val returnedAd = rows.firstOrNull()
        when {
            returnedAd == null -> errorNotFound(rq.ad.id)
            returnedAd.lock == newAd.lock -> DbAdResponseOk(returnedAd)
            else -> errorRepoConcurrency(returnedAd, rq.ad.lock)
        }
    }

    override suspend fun deleteAd(rq: DbAdIdRequest): IDbAdResponse = tryAdMethod {
        val id = rq.id.takeIf { it != MkplAdId.NONE } ?: return@tryAdMethod errorEmptyId
        val oldLock = rq.lock.takeIf { it != MkplAdLock.NONE } ?: return@tryAdMethod errorEmptyLock(id)

        // 1. Читаем текущее состояние
        val current = fetchById(id)
        when {
            current == null -> errorNotFound(id)
            current.lock == MkplAdLock.NONE -> errorDb(RepoEmptyLockException(id))
            current.lock != oldLock -> errorRepoConcurrency(current, oldLock)
            else -> {
                // 2. Удаляем с проверкой lock
                val stmt = Statement.create("DELETE FROM $dbName WHERE ${SqlFields.ID.quoted()} = :id AND ${SqlFields.LOCK.quoted()} = :lock")
                    .bind("id", id.asString())
                    .bind("lock", oldLock.asString())
                db.execute(stmt).getOrThrow()
                DbAdResponseOk(current)
            }
        }
    }

    override suspend fun searchAd(rq: DbAdFilterRequest): IDbAdsResponse = tryAdsMethod {
        val sql = SqlQueryBuilder.search(
            dbName, cols,
            ownerId = rq.ownerId != MkplUserId.NONE,
            dealSide = rq.dealSide != MkplDealSide.NONE,
            titleFilter = rq.titleFilter.isNotBlank(),
        )
        val stmt = Statement.create(sql)
        if (rq.ownerId != MkplUserId.NONE)
            stmt.bind(SqlFields.OWNER_ID, rq.ownerId.asString())
        if (rq.dealSide != MkplDealSide.NONE)
            stmt.bind(SqlFields.AD_TYPE, rq.dealSide.toDbString())
        if (rq.titleFilter.isNotBlank())
            stmt.bind(SqlFields.TITLE, "%${rq.titleFilter}%")
        val rows: List<MkplAd> = db.fetchAll(stmt, MkplAdRowMapper).getOrThrow()
        DbAdsResponseOk(data = rows)
    }

    override fun save(ads: Collection<MkplAd>): Collection<MkplAd> = runBlocking {
        ads.map { ad ->
            val stmt = Statement.create(SqlQueryBuilder.insert(dbName, cols)).bindAd(ad)
            val rows: List<MkplAd> = db.fetchAll(stmt, MkplAdRowMapper).getOrThrow()
            rows.first()
        }
    }

    fun clear(): Unit = runBlocking {
        db.execute(Statement.create(SqlQueryBuilder.clear(dbName))).getOrThrow()
    }

    private suspend fun fetchById(id: MkplAdId): MkplAd? {
        val stmt = Statement.create("SELECT $cols FROM $dbName WHERE ${SqlFields.ID.quoted()} = :id")
            .bind("id", id.asString())
        val rows: List<MkplAd> = db.fetchAll(stmt, MkplAdRowMapper).getOrThrow()
        return rows.firstOrNull()
    }

    private suspend fun tryAdMethod(block: suspend () -> IDbAdResponse): IDbAdResponse = try {
        block()
    } catch (e: Throwable) {
        DbAdResponseErr(errorSystem("methodException", e = e))
    }

    private suspend fun tryAdsMethod(block: suspend () -> IDbAdsResponse): IDbAdsResponse = try {
        block()
    } catch (e: Throwable) {
        DbAdsResponseErr(errorSystem("methodException", e = e))
    }
}
