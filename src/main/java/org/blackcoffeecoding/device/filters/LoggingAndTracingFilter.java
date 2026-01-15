package org.blackcoffeecoding.device.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(1) // Сработает первым [cite: 526]
public class LoggingAndTracingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(LoggingAndTracingFilter.class);
    private static final String CORRELATION_ID_HEADER = "X-Request-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 1. Управление Correlation ID
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (!StringUtils.hasText(correlationId)) {
            correlationId = UUID.randomUUID().toString(); // Генерируем новый, если нет [cite: 530]
        }

        // Кладем в MDC (контекст логирования) [cite: 531]
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        // Добавляем в ответ клиенту, чтобы он тоже знал ID [cite: 532]
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        // Задание 2: Логируем только запросы к API [cite: 564]
        boolean isApiRequest = request.getRequestURI().startsWith("/api/");

        long startTime = System.currentTimeMillis();

        try {
            if (isApiRequest) {
                log.info("Request started: {} {}", request.getMethod(), request.getRequestURI());
            }

            // Передаем управление дальше (другим фильтрам и контроллеру)
            filterChain.doFilter(request, response);

        } finally {
            // Этот блок выполнится ПОСЛЕ отработки контроллера
            long duration = System.currentTimeMillis() - startTime;

            if (isApiRequest) {
                log.info("Request finished: {} {} with status {} in {}ms",
                        request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
            }

            // Критически важно очистить MDC! [cite: 537]
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }
}