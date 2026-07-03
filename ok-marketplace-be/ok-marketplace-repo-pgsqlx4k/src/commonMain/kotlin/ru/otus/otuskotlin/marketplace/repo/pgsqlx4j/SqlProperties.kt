package ru.otus.otuskotlin.marketplace.repo.pgsqlx4k

data class SqlProperties(
    val host: String = "localhost",
    val port: Int = 5432,
    val user: String = "postgres",
    val password: String = "marketplace-pass",
    val database: String = "marketplace_ads",
    val schema: String = "public",
    val table: String = "ads",
    val maxConnections: Int = 10,
) {
    val url: String
        get() = "postgresql://$host:$port/$database"
}
