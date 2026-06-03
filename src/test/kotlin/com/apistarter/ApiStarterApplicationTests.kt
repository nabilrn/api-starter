package com.apistarter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ApiStarterApplicationTests {
    @Test
    fun `application class can be instantiated`() {
        assertThat(ApiStarterApplication()).isNotNull()
    }
}
