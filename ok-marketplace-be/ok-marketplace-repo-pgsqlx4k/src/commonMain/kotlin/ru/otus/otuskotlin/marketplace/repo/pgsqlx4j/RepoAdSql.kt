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
    properties: SqlProperties,
    private val randomUuid: () -> String = { uuid4().toString() },
) : IRepoAd, IRepoAdInitializable {

    private val db = postgreSQL(
        url = properties.url,
        username = properties.user,
        password = properties.password,
        options = ConnectionPool.Options(maxConnections = properties.maxConnections),
    )

    private val tableRef = "${properties.schema}.${properties.table}"
    private val allColumns = SqlFields.columns()

    override fun save(ads: Collection<MkplAd>): Collection<MkplAd> = runBlocking {
        ads.map { ad ->
            val stmt = buildInsertStmt(ad)
            val rows: List<MkplAd> = db.fetchAll(stmt, MkplAdRowMapper).getOrThrow()
            rows.first()
        }
    }

    fun clear(): Unit = runBlocking {
        db.execute(Statement.create("DELETE FROM $tableRef;")).getOrThrow()
    }

    override suspend fun createAd(rq: DbAdRequest): IDbAdResponse = tryAdMethod {
        val newId = randomUuid()
        val ad = rq.ad.copy(id = MkplAdId(newId), lock = MkplAdLock(randomUuid()))
        val stmt = buildInsertStmt(ad)
        val rows: List<MkplAd> = db.fetchAll(stmt, MkplAdRowMapper).getOrThrow()
        if (rows.isEmpty()) throw RuntimeException("DB error: insert returned no rows")
        DbAdResponseOk(rows.first())
    }

    override suspend fun readAd(rq: DbAdIdRequest): IDbAdResponse = tryAdMethod {
        val idStr = rq.id.takeIf { it != MkplAdId.NONE }?.asString() ?: return@tryAdMethod errorEmptyId
        val stmt = Statement.create("SELECT $allColumns FROM $tableRef WHERE ${SqlFields.ID} = :id")
            .bind("id", idStr)
        val rows: List<MkplAd> = db.fetchAll(stmt, MkplAdRowMapper).getOrThrow()
        if (rows.isEmpty()) errorNotFound(rq.id)
        else DbAdResponseOk(rows.first())
    }

    override suspend fun updateAd(rq: DbAdRequest): IDbAdResponse = tryAdMethod {
        val rqAd = rq.ad
        val adId = rqAd.id.takeIf { it != MkplAdId.NONE } ?: return@tryAdMethod errorEmptyId
        val expectedLock = rqAd.lock.takeIf { it != MkplAdLock.NONE } ?: return@tryAdMethod errorEmptyLock(adId)
        val key = adId.asString()

        val current: MkplAd? = findById(key)
        when {
            current == null -> errorNotFound(adId)
            current.lock == MkplAdLock.NONE -> errorDb(RepoEmptyLockException(adId))
            current.lock != expectedLock -> errorRepoConcurrency(current, expectedLock)
            else -> {
                val newLock = MkplAdLock(randomUuid())
                val updateStmt = Statement.create(
                    """
                    UPDATE $tableRef SET
                        ${SqlFields.TITLE} = :title,
                        ${SqlFields.DESCRIPTION} = :description,
                        ${SqlFields.AD_TYPE} = :adType,
                        ${SqlFields.VISIBILITY} = :visibility,
                        ${SqlFields.LOCK} = :newLock,
                        ${SqlFields.OWNER_ID} = :ownerId,
                        ${SqlFields.PRODUCT_ID} = :productId
                    WHERE ${SqlFields.ID} = :id AND ${SqlFields.LOCK} = :expectedLock
                    RETURNING $allColumns
                    """.trimIndent()
                )
                    .bind("id", key)
                    .bind("expectedLock", expectedLock.asString())
                    .bind("title", rqAd.title)
                    .bind("description", rqAd.description)
                    .bind("adType", rqAd.adType.toDbString())
                    .bind("visibility", rqAd.visibility.toDbString())
                    .bind("newLock", newLock.asString())
                    .bind("ownerId", rqAd.ownerId.asString())
                    .bind("productId", rqAd.productId.takeIf { it != MkplProductId.NONE }?.asString() ?: "")
                val updatedRows: List<MkplAd> = db.fetchAll(updateStmt, MkplAdRowMapper).getOrThrow()
                if (updatedRows.isEmpty()) errorNotFound(adId)
                else DbAdResponseOk(updatedRows.first())
            }
        }
    }

    override suspend fun deleteAd(rq: DbAdIdRequest): IDbAdResponse = tryAdMethod {
        val adId = rq.id.takeIf { it != MkplAdId.NONE } ?: return@tryAdMethod errorEmptyId
        val expectedLock = rq.lock.takeIf { it != MkplAdLock.NONE } ?: return@tryAdMethod errorEmptyLock(adId)
        val key = adId.asString()

        val current: MkplAd? = findById(key)
        when {
            current == null -> errorNotFound(adId)
            current.lock == MkplAdLock.NONE -> errorDb(RepoEmptyLockException(adId))
            current.lock != expectedLock -> errorRepoConcurrency(current, expectedLock)
            else -> {
                db.execute(
                    Statement.create("DELETE FROM $tableRef WHERE ${SqlFields.ID} = :id AND ${SqlFields.LOCK} = :lock")
                        .bind("id", key)
                        .bind("lock", expectedLock.asString())
                ).getOrThrow()
                DbAdResponseOk(current)
            }
        }
    }

    override suspend fun searchAd(rq: DbAdFilterRequest): IDbAdsResponse = tryAdsMethod {
        val conditions = mutableListOf<String>()
        val params = mutableMapOf<String, String>()

        if (rq.ownerId != MkplUserId.NONE) {
            conditions.add("${SqlFields.OWNER_ID} = :ownerId")
            params["ownerId"] = rq.ownerId.asString()
        }
        if (rq.dealSide != MkplDealSide.NONE) {
            conditions.add("${SqlFields.AD_TYPE} = :dealSide")
            params["dealSide"] = rq.dealSide.toDbString()
        }
        if (rq.titleFilter.isNotBlank()) {
            conditions.add("(${SqlFields.TITLE} LIKE :titleFilter OR ${SqlFields.DESCRIPTION} LIKE :titleFilter)")
            params["titleFilter"] = "%${rq.titleFilter}%"
        }

        val sql = StringBuilder("SELECT $allColumns FROM $tableRef")
        if (conditions.isNotEmpty()) {
            sql.append(" WHERE ${conditions.joinToString(" AND ")}")
        }

        val stmt = Statement.create(sql.toString())
        params.forEach { (k, v) -> stmt.bind(k, v) }

        val rows: List<MkplAd> = db.fetchAll(stmt, MkplAdRowMapper).getOrThrow()
        DbAdsResponseOk(data = rows)
    }

    private fun findById(key: String): MkplAd? {
        val stmt = Statement.create("SELECT $allColumns FROM $tableRef WHERE ${SqlFields.ID} = :id")
            .bind("id", key)
        val rows: List<MkplAd> = runBlocking {
            db.fetchAll(stmt, MkplAdRowMapper).getOrThrow()
        }
        return rows.firstOrNull()
    }

    private fun buildInsertStmt(ad: MkplAd): Statement {
        val id = ad.id.takeIf { it != MkplAdId.NONE }?.asString() ?: randomUuid()
        val lock = ad.lock.takeIf { it != MkplAdLock.NONE }?.asString() ?: randomUuid()
        return Statement.create(
            """
            INSERT INTO $tableRef ($allColumns)
            VALUES (:id, :title, :description, :adType, :visibility, :lock, :ownerId, :productId)
            RETURNING $allColumns
            """.trimIndent()
        )
            .bind("id", id)
            .bind("title", ad.title)
            .bind("description", ad.description)
            .bind("adType", ad.adType.toDbString())
            .bind("visibility", ad.visibility.toDbString())
            .bind("lock", lock)
            .bind("ownerId", ad.ownerId.asString())
            .bind("productId", ad.productId.takeIf { it != MkplProductId.NONE }?.asString() ?: "")
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

private fun MkplDealSide.toDbString(): String = when (this) {
    MkplDealSide.DEMAND -> SqlFields.AD_TYPE_DEMAND
    MkplDealSide.SUPPLY -> SqlFields.AD_TYPE_SUPPLY
    MkplDealSide.NONE -> throw Exception("Wrong value of Ad Type. NONE is unsupported")
}

private fun MkplVisibility.toDbString(): String = when (this) {
    MkplVisibility.VISIBLE_PUBLIC -> SqlFields.VISIBILITY_PUBLIC
    MkplVisibility.VISIBLE_TO_OWNER -> SqlFields.VISIBILITY_OWNER
    MkplVisibility.VISIBLE_TO_GROUP -> SqlFields.VISIBILITY_GROUP
    MkplVisibility.NONE -> throw Exception("Wrong value of Visibility. NONE is unsupported")
}
