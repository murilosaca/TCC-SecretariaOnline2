package br.ufpr.sept.so2.shared.api

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.domain.Page
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.function.Function

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PageResponse<T>(
    val content: List<T>,
    val page: PageMeta,
    @get:JsonProperty("_links")
    val links: PageLinks?,
) {
    companion object {
        @JvmStatic
        fun <T> of(page: Page<T>): PageResponse<T> = ofWithLinks(page, Function.identity())

        @JvmStatic
        @JvmOverloads
        fun <T, R> ofWithLinks(
            page: Page<T>,
            mapper: Function<T, R>,
            extraLinks: Map<String, String> = emptyMap(),
        ): PageResponse<R> {
            val base = currentBaseUri()
            val pageLinks = if (base == null) null else buildLinks(base, page, extraLinks)
            return PageResponse(
                page.content.map(mapper::apply),
                PageMeta(page.number, page.size, page.totalElements, page.totalPages),
                pageLinks,
            )
        }

        private fun currentBaseUri(): String? =
            try {
                ServletUriComponentsBuilder.fromCurrentRequest()
                    .replaceQueryParam("page")
                    .replaceQueryParam("size")
                    .build()
                    .toUriString()
                    .replace(Regex("[?&]$"), "")
            } catch (_: IllegalStateException) {
                null
            }

        private fun buildLinks(base: String, page: Page<*>, extraLinks: Map<String, String>): PageLinks {
            val last = maxOf(page.totalPages - 1, 0)
            return PageLinks(
                self = pageUrl(base, page.number, page.size),
                first = pageUrl(base, 0, page.size),
                last = pageUrl(base, last, page.size),
                next = if (page.hasNext()) pageUrl(base, page.number + 1, page.size) else null,
                prev = if (page.hasPrevious()) pageUrl(base, page.number - 1, page.size) else null,
                extra = extraLinks,
            )
        }

        private fun pageUrl(base: String, page: Int, size: Int): String {
            val separator = if (base.contains("?")) "&" else "?"
            return "$base${separator}page=$page&size=$size"
        }
    }

    data class PageMeta(
        val number: Int,
        val size: Int,
        val totalElements: Long,
        val totalPages: Int,
    )

    data class PageLinks(
        val self: String,
        val first: String,
        val last: String,
        val next: String?,
        val prev: String?,
        @get:JsonAnyGetter
        val extra: Map<String, String> = emptyMap(),
    ) {
        constructor(self: String, first: String, last: String, next: String?, prev: String?) :
            this(self, first, last, next, prev, emptyMap())
    }
}
