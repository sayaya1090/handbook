package dev.sayaya.handbook.domain

@JvmRecord
data class Search(
    val page: Int,
    val limit: Int,
    val sortBy: String?,
    val asc: Boolean?,
    val filters: List<Pair<String, Any?>> = emptyList(),
) {
    init {
        require(page >= 0) { "Page must be a non-negative integer. Given: $page" }
        require(asc == null || sortBy != null) {
            "If 'asc' is not null, 'sortBy' must also be provided. Given: asc=$asc, sortBy=$sortBy"
        }
    }
}
