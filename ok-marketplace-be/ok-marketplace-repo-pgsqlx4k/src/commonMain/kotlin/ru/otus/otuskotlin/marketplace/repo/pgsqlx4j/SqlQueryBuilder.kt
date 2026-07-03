package ru.otus.otuskotlin.marketplace.repo.pgsqlx4k

object SqlQueryBuilder {

    fun insert(dbName: String, cols: String): String = """
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

    fun read(dbName: String, cols: String): String = """
        SELECT $cols
        FROM $dbName
        WHERE ${SqlFields.ID.quoted()} = :${SqlFields.ID}
        """.trimIndent()

    fun update(dbName: String, cols: String): String = """
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

    fun delete(dbName: String, cols: String): String = """
        WITH delete_obj AS (
            DELETE FROM $dbName a
            WHERE  a.${SqlFields.ID.quoted()} = :${SqlFields.ID}
            AND a.${SqlFields.LOCK.quoted()} = :${SqlFields.LOCK_OLD}
            RETURNING '${SqlFields.DELETE_OK}'
        )
        SELECT $cols, (SELECT * FROM delete_obj) as flag FROM $dbName
        WHERE ${SqlFields.ID.quoted()} = :${SqlFields.ID}
        """.trimIndent()

    fun search(dbName: String, cols: String, ownerId: Boolean, dealSide: Boolean, titleFilter: Boolean): String {
        val where = listOfNotNull(
            if (ownerId) "${SqlFields.OWNER_ID.quoted()} = :${SqlFields.OWNER_ID}" else null,
            if (dealSide) "${SqlFields.AD_TYPE.quoted()} = CAST(:${SqlFields.AD_TYPE} AS ${SqlFields.AD_TYPE_TYPE})" else null,
            if (titleFilter) "${SqlFields.TITLE.quoted()} LIKE :${SqlFields.TITLE}" else null,
        )
            .takeIf { it.isNotEmpty() }
            ?.let { "WHERE ${it.joinToString(separator = " AND ")}" }
            ?: ""
        return """
            SELECT $cols
            FROM $dbName $where
            """.trimIndent()
    }

    fun clear(dbName: String): String = "DELETE FROM $dbName;"
}
