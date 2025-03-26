package dev.sayaya.handbook.domain

@JvmRecord
data class Search (
    val page: Int,
    val limit: Int,
    val sortBy: String?,
    val asc: Boolean?,
    val filters: List<Pair<String, String>> = emptyList()
) {
    init {
        require(page >= 0) { "Page must be a non-negative integer. Given: $page" }
        require(limit in 1..100) {
            "Limit must be greater than 0, and less than or equal to 100. Given: $limit"
        }
        require(asc == null || sortBy != null) {
            "If 'asc' is not null, 'sortBy' must also be provided. Given: asc=$asc, sortBy=$sortBy"
        }
    }
}