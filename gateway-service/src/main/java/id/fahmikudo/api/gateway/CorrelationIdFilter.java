package id.fahmikudo.api.gateway;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.slf4j.MDC;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String correlationId = request.getHeader(HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        request.setAttribute(HEADER, correlationId);
        response.setHeader(HEADER, correlationId);
        MDC.put(HEADER, correlationId);
        try {
            HttpServletRequest wrappedRequest = new CorrelationIdRequestWrapper(request, correlationId);
            filterChain.doFilter(wrappedRequest, response);
        } finally {
            MDC.remove(HEADER);
        }
    }

    private static final class CorrelationIdRequestWrapper extends HttpServletRequestWrapper {

        private final String correlationId;

        CorrelationIdRequestWrapper(HttpServletRequest request, String correlationId) {
            super(request);
            this.correlationId = correlationId;
        }

        @Override
        public String getHeader(String name) {
            if (HEADER.equalsIgnoreCase(name)) {
                return correlationId;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (HEADER.equalsIgnoreCase(name)) {
                return Collections.enumeration(Collections.singleton(correlationId));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Enumeration<String> headerNames = super.getHeaderNames();
            return new Enumeration<>() {
                private boolean correlationIdReturned;

                @Override
                public boolean hasMoreElements() {
                    return headerNames.hasMoreElements() || !correlationIdReturned;
                }

                @Override
                public String nextElement() {
                    if (headerNames.hasMoreElements()) {
                        return headerNames.nextElement();
                    }
                    if (!correlationIdReturned) {
                        correlationIdReturned = true;
                        return HEADER;
                    }
                    throw new java.util.NoSuchElementException();
                }
            };
        }
    }
}
