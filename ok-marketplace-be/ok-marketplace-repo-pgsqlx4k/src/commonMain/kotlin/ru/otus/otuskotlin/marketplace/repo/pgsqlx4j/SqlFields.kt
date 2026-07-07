package ru.otus.otuskotlin.marketplace.repo.pgsqlx4k

object SqlFields {
    const val ID = "id"
    const val TITLE = "title"
    const val DESCRIPTION = "description"
    const val AD_TYPE = "ad_type"
    const val VISIBILITY = "visibility"
    const val LOCK = "lock"
    const val LOCK_OLD = "lock_old"
    const val OWNER_ID = "owner_id"
    const val PRODUCT_ID = "product_id"

    const val AD_TYPE_TYPE = "ad_types_type"
    const val AD_TYPE_DEMAND = "demand"
    const val AD_TYPE_SUPPLY = "supply"

    const val VISIBILITY_TYPE = "ad_visibilities_type"
    const val VISIBILITY_PUBLIC = "public"
    const val VISIBILITY_OWNER = "owner"
    const val VISIBILITY_GROUP = "group"

    const val DELETE_OK = "DELETE_OK"

    val allFields = listOf(
        ID, TITLE, DESCRIPTION, AD_TYPE, VISIBILITY, LOCK, OWNER_ID, PRODUCT_ID,
    )
}

internal fun String.quoted() = "\"$this\""
