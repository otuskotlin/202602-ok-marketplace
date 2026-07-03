package ru.otus.otuskotlin.marketplace.repo.pgsqlx4k

import com.benasher44.uuid.uuid4
import io.github.smyrgeorge.sqlx4k.ConnectionPool
import io.github.smyrgeorge.sqlx4k.Statement
import io.github.smyrgeorge.sqlx4k.postgres.postgreSQL
import kotlinx.coroutines.runBlocking
import ru.otus.otuskotlin.marketplace.common.helpers.errorSystem
import ru.otus.otuskotlin.marketplace.common.models.*
import ru.otus.otuskotlin.marketplace.common.repo.*
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
        val stmt = Statement.create(
            """
            INSERT INTO $dbName (
              ${SqlFields.ID.quoted()},
              ${SqlFields.TITLE.quoted()},
              ${SqlFields.DESCRIPTION.quoted()},
              ${SqlFields.VISIBILITY.quoted()},
              ${SqlFields.AD_TYPE.quoted()},
              ${SqlFields.LOCK.quoted()},
              ${SqlFields.OWNER_ID.quoted()},
              ${SqlFields.PRODUCT_ID.quoted()}
            ) VALUES (
              :${SqlFields.ID},
              :${SqlFields.TITLE},
              :${SqlFields.DESCRIPTION},
              CAST(:${SqlFields.VISIBILITY} AS ${SqlFields.VISIBILITY_TYPE}),
              CAST(:${SqlFields.AD_TYPE} AS ${SqlFields.AD_TYPE_TYPE}),
              :${SqlFields.LOCK},
              :${SqlFields.OWNER_ID},
              :${SqlFields.PRODUCT_ID}
            )
            RETURNING $cols
            """.trimIndent()
        ).bindAd(ad)
        val rows: List<MkplAd> = db.fetchAll(stmt, MkplAdRowMapper).getOrThrow()
        if (rows.isEmpty()) throw RuntimeException("DB error: insert returned no rows")
        DbAdResponseOk(rows.first())
    }

    override suspend fun readAd(rq: DbAdIdRequest): IDbAdResponse = tryAdMethod {
        val sql = """
            SELECT $cols
            FROM $dbName
            WHERE ${SqlFields.ID.quoted()} = :${SqlFields.ID}
            """.trimIndent()
        val stmt = Statement.create(sql)
            .bind(SqlFields.ID, rq.id.asString())
        val rows: List<MkplAd> = db.fetchAll(stmt, MkplAdRowMapper).getOrThrow()
        if (rows.isEmpty()) errorNotFound(rq.id)
        else DbAdResponseOk(rows.first())
    }

    override suspend fun updateAd(rq: DbAdRequest): IDbAdResponse = tryAdMethod {
        val rqAd = rq.ad
        val newAd = rqAd.copy(lock = MkplAdLock(randomUuid()))
        val sql = """
            WITH update_obj AS (
                UPDATE $dbName a
                SET ${SqlFields.TITLE.quoted()} = :${SqlFields.TITLE}
                , ${SqlFields.DESCRIPTION.quoted()} = :${SqlFields.DESCRIPTION}
                , ${SqlFields.AD_TYPE.quoted()} = CAST(:${SqlFields.AD_TYPE} AS ${SqlFields.AD_TYPE_TYPE})
                , ${SqlFields.VISIBILITY.quoted()} = CAST(:${SqlFields.VISIBILITY} AS ${SqlFields.VISIBILITY_TYPE})
                , ${SqlFields.LOCK.quoted()} = :${SqlFields.LOCK}
                , ${SqlFields.OWNER_ID.quoted()} = :${SqlFields.OWNER_ID}
                , ${SqlFields.PRODUCT_ID.quoted()} = :${SqlFields.PRODUCT_ID}
                WHERE  a.${SqlFields.ID.quoted()} = :${SqlFields.ID}
                AND a.${SqlFields.LOCK.quoted()} = :${SqlFields.LOCK_OLD}
                RETURNING $cols
            ),
            select_obj AS (
                SELECT $cols FROM $dbName
                WHERE ${SqlFields.ID.quoted()} = :${SqlFields.ID}
            )
            (SELECT * FROM update_obj UNION ALL SELECT * FROM select_obj) LIMIT 1
            """.trimIndent()
        val stmt = Statement.create(sql)
            .bindAd(newAd)
            .bind(SqlFields.LOCK_OLD, rqAd.lock.asString())
        val rows: List<MkplAd> = db.fetchAll(stmt, MkplAdRowMapper).getOrThrow()
        val returnedAd = rows.firstOrNull()
        when {
            returnedAd == null -> errorNotFound(rqAd.id)
            returnedAd.lock == newAd.lock -> DbAdResponseOk(returnedAd)
            else -> errorRepoConcurrency(returnedAd, rqAd.lock)
        }
    }

    override suspend fun deleteAd(rq: DbAdIdRequest): IDbAdResponse = tryAdMethod {
        val sql = """
            WITH delete_obj AS (
                DELETE FROM $dbName a
                WHERE  a.${SqlFields.ID.quoted()} = :${SqlFields.ID}
                AND a.${SqlFields.LOCK.quoted()} = :${SqlFields.LOCK_OLD}
                RETURNING '${SqlFields.DELETE_OK}'
            )
            SELECT $cols, (SELECT * FROM delete_obj) as flag FROM $dbName
            WHERE ${SqlFields.ID.quoted()} = :${SqlFields.ID}
            """.trimIndent()
        val stmt = Statement.create(sql)
            .bind(SqlFields.ID, rq.id.asString())
            .bind(SqlFields.LOCK_OLD, rq.lock.asString())
        val rows = db.fetchAll(stmt, MkplAdRowMapper).getOrThrow()
        val returnedAd = rows.firstOrNull()
        when {
            returnedAd == null -> errorNotFound(rq.id)
            else -> DbAdResponseOk(returnedAd)
        }
    }

    override suspend fun searchAd(rq: DbAdFilterRequest): IDbAdsResponse = tryAdsMethod {
        val where = listOfNotNull(
            rq.ownerId.takeIf { it != MkplUserId.NONE }
                ?.let { "${SqlFields.OWNER_ID.quoted()} = :${SqlFields.OWNER_ID}" },
            rq.dealSide.takeIf { it != MkplDealSide.NONE }
                ?.let { "${SqlFields.AD_TYPE.quoted()} = CAST(:${SqlFields.AD_TYPE} AS ${SqlFields.AD_TYPE_TYPE})" },
            rq.titleFilter.takeIf { it.isNotBlank() }
                ?.let { "${SqlFields.TITLE.quoted()} LIKE :${SqlFields.TITLE}" },
        )
            .takeIf { it.isNotEmpty() }
            ?.let { "WHERE ${it.joinToString(separator = " AND ")}" }
            ?: ""

        val sql = """
            SELECT $cols
            FROM $dbName $where
            """.trimIndent()

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
            val stmt = Statement.create(
                """
                INSERT INTO $dbName (
                  ${SqlFields.ID.quoted()},
                  ${SqlFields.TITLE.quoted()},
                  ${SqlFields.DESCRIPTION.quoted()},
                  ${SqlFields.VISIBILITY.quoted()},
                  ${SqlFields.AD_TYPE.quoted()},
                  ${SqlFields.LOCK.quoted()},
                  ${SqlFields.OWNER_ID.quoted()},
                  ${SqlFields.PRODUCT_ID.quoted()}
                ) VALUES (
                  :${SqlFields.ID},
                  :${SqlFields.TITLE},
                  :${SqlFields.DESCRIPTION},
                  CAST(:${SqlFields.VISIBILITY} AS ${SqlFields.VISIBILITY_TYPE}),
                  CAST(:${SqlFields.AD_TYPE} AS ${SqlFields.AD_TYPE_TYPE}),
                  :${SqlFields.LOCK},
                  :${SqlFields.OWNER_ID},
                  :${SqlFields.PRODUCT_ID}
                )
                RETURNING $cols
                """.trimIndent()
            ).bindAd(ad)
            val rows: List<MkplAd> = db.fetchAll(stmt, MkplAdRowMapper).getOrThrow()
            rows.first()
        }
    }

    fun clear(): Unit = runBlocking {
        db.execute(Statement.create("DELETE FROM $dbName;")).getOrThrow()
    }

    private fun Statement.bindAd(ad: MkplAd): Statement = apply {
        bind(SqlFields.ID, ad.id.asString())
        bind(SqlFields.TITLE, ad.title)
        bind(SqlFields.DESCRIPTION, ad.description)
        bind(SqlFields.AD_TYPE, ad.adType.toDbString())
        bind(SqlFields.VISIBILITY, ad.visibility.toDbString())
        bind(SqlFields.LOCK, ad.lock.asString())
        bind(SqlFields.OWNER_ID, ad.ownerId.asString())
        bind(SqlFields.PRODUCT_ID, ad.productId.takeIf { it != MkplProductId.NONE }?.asString() ?: "")
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
