package br.ufpr.sept.so2.shared.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.function.Function;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {

    private final List<T> content;
    private final PageMeta page;
    @JsonProperty("_links")
    private final PageLinks links;

    public PageResponse(List<T> content, PageMeta page, PageLinks links) {
        this.content = content;
        this.page = page;
        this.links = links;
    }

    public static <T> PageResponse<T> of(Page<T> page) {
        return ofWithLinks(page, Function.identity());
    }

    public static <T, R> PageResponse<R> ofWithLinks(Page<T> page, Function<T, R> mapper) {
        String base = currentBaseUri();
        PageLinks pageLinks = base == null ? null : buildLinks(base, page);
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                new PageMeta(page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages()),
                pageLinks
        );
    }

    public List<T> getContent() {
        return content;
    }

    public PageMeta getPage() {
        return page;
    }

    public PageLinks getLinks() {
        return links;
    }

    private static String currentBaseUri() {
        try {
            String uri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .replaceQueryParam("page")
                    .replaceQueryParam("size")
                    .build()
                    .toUriString();
            return uri.replaceAll("[?&]$", "");
        } catch (IllegalStateException ignored) {
            return null;
        }
    }

    private static PageLinks buildLinks(String base, Page<?> page) {
        int last = Math.max(page.getTotalPages() - 1, 0);
        return new PageLinks(
                pageUrl(base, page.getNumber(), page.getSize()),
                pageUrl(base, 0, page.getSize()),
                pageUrl(base, last, page.getSize()),
                page.hasNext() ? pageUrl(base, page.getNumber() + 1, page.getSize()) : null,
                page.hasPrevious() ? pageUrl(base, page.getNumber() - 1, page.getSize()) : null
        );
    }

    private static String pageUrl(String base, int page, int size) {
        String separator = base.contains("?") ? "&" : "?";
        return base + separator + "page=" + page + "&size=" + size;
    }

    public static final class PageMeta {

        private final int number;
        private final int size;
        private final long totalElements;
        private final int totalPages;

        public PageMeta(int number, int size, long totalElements, int totalPages) {
            this.number = number;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
        }

        public int getNumber() {
            return number;
        }

        public int getSize() {
            return size;
        }

        public long getTotalElements() {
            return totalElements;
        }

        public int getTotalPages() {
            return totalPages;
        }
    }

    public static final class PageLinks {

        private final String self;
        private final String first;
        private final String last;
        private final String next;
        private final String prev;

        public PageLinks(String self, String first, String last, String next, String prev) {
            this.self = self;
            this.first = first;
            this.last = last;
            this.next = next;
            this.prev = prev;
        }

        public String getSelf() {
            return self;
        }

        public String getFirst() {
            return first;
        }

        public String getLast() {
            return last;
        }

        public String getNext() {
            return next;
        }

        public String getPrev() {
            return prev;
        }
    }
}
