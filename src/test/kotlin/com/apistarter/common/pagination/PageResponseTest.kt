package com.apistarter.common.pagination

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class PageResponseTest {
    @Test
    fun `from maps spring page metadata`() {
        val page = PageImpl(
            listOf("one", "two"),
            PageRequest.of(1, 2),
            5,
        )

        val response = PageResponse.from(page)

        assertThat(response.items).containsExactly("one", "two")
        assertThat(response.page).isEqualTo(1)
        assertThat(response.size).isEqualTo(2)
        assertThat(response.totalItems).isEqualTo(5)
        assertThat(response.totalPages).isEqualTo(3)
        assertThat(response.hasNext).isTrue()
        assertThat(response.hasPrevious).isTrue()
    }
}
