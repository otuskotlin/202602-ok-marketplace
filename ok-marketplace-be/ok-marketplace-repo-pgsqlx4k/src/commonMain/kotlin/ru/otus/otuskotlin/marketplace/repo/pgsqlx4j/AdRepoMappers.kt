package ru.otus.otuskotlin.marketplace.repo.pgsqlx4k

import io.github.smyrgeorge.sqlx4k.Statement
import ru.otus.otuskotlin.marketplace.common.models.*

internal fun Statement.bindAd(ad: MkplAd): Statement = apply {
    bind(SqlFields.ID, ad.id.asString())
    bind(SqlFields.TITLE, ad.title)
    bind(SqlFields.DESCRIPTION, ad.description)
    bind(SqlFields.AD_TYPE, ad.adType.toDbString())
    bind(SqlFields.VISIBILITY, ad.visibility.toDbString())
    bind(SqlFields.LOCK, ad.lock.asString())
    bind(SqlFields.OWNER_ID, ad.ownerId.asString())
    bind(SqlFields.PRODUCT_ID, ad.productId.takeIf { it != MkplProductId.NONE }?.asString() ?: "")
}

internal fun MkplDealSide.toDbString(): String = when (this) {
    MkplDealSide.DEMAND -> SqlFields.AD_TYPE_DEMAND
    MkplDealSide.SUPPLY -> SqlFields.AD_TYPE_SUPPLY
    MkplDealSide.NONE -> throw Exception("Wrong value of Ad Type. NONE is unsupported")
}

internal fun MkplVisibility.toDbString(): String = when (this) {
    MkplVisibility.VISIBLE_PUBLIC -> SqlFields.VISIBILITY_PUBLIC
    MkplVisibility.VISIBLE_TO_OWNER -> SqlFields.VISIBILITY_OWNER
    MkplVisibility.VISIBLE_TO_GROUP -> SqlFields.VISIBILITY_GROUP
    MkplVisibility.NONE -> throw Exception("Wrong value of Visibility. NONE is unsupported")
}
