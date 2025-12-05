package id.fahmikudo.api.gateway;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void shouldGenerateAndPropagateCorrelationId() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        String correlationIdRequestAttribute = (String) request.getAttribute(CorrelationIdFilter.HEADER);
        assertThat(correlationIdRequestAttribute).isNotBlank();
        assertThat(response.getHeader(CorrelationIdFilter.HEADER))
                .isEqualTo(correlationIdRequestAttribute);
    }
}

