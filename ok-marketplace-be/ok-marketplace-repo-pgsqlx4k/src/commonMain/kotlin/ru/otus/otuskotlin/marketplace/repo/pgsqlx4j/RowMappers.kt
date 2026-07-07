package ru.otus.otuskotlin.marketplace.repo.pgsqlx4k

import io.github.smyrgeorge.sqlx4k.RowMapper
import io.github.smyrgeorge.sqlx4k.ValueEncoderRegistry
import io.github.smyrgeorge.sqlx4k.ResultSet
import ru.otus.otuskotlin.marketplace.common.models.*

object MkplAdRowMapper : RowMapper<MkplAd> {
    override fun map(row: ResultSet.Row, converters: ValueEncoderRegistry): MkplAd {
        return MkplAd(
            id = MkplAdId(row.get(SqlFields.ID).asString()),
            title = row.get(SqlFields.TITLE).asString() ?: "",
            description = row.get(SqlFields.DESCRIPTION).asString() ?: "",
            ownerId = MkplUserId(row.get(SqlFields.OWNER_ID).asString()),
            adType = parseDealSide(row.get(SqlFields.AD_TYPE).asString()),
            visibility = parseVisibility(row.get(SqlFields.VISIBILITY).asString()),
            lock = MkplAdLock(row.get(SqlFields.LOCK).asString()),
            productId = row.get(SqlFields.PRODUCT_ID).asString()
                ?.takeIf { it.isNotBlank() }
                ?.let { MkplProductId(it) }
                ?: MkplProductId.NONE,
        )
    }

    private fun parseDealSide(value: String): MkplDealSide = when (value) {
        SqlFields.AD_TYPE_DEMAND -> MkplDealSide.DEMAND
        SqlFields.AD_TYPE_SUPPLY -> MkplDealSide.SUPPLY
        else -> MkplDealSide.NONE
    }

    private fun parseVisibility(value: String): MkplVisibility = when (value) {
        SqlFields.VISIBILITY_PUBLIC -> MkplVisibility.VISIBLE_PUBLIC
        SqlFields.VISIBILITY_OWNER -> MkplVisibility.VISIBLE_TO_OWNER
        SqlFields.VISIBILITY_GROUP -> MkplVisibility.VISIBLE_TO_GROUP
        else -> MkplVisibility.NONE
    }
}
