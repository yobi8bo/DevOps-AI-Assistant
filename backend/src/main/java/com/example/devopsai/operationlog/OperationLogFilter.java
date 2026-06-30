package com.example.devopsai.operationlog;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order
public class OperationLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(OperationLogFilter.class);

    private final OperationLogService operationLogService;

    public OperationLogFilter(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Throwable failure = null;
        try {
            filterChain.doFilter(request, response);
        } catch (ServletException | IOException | RuntimeException ex) {
            failure = ex;
            throw ex;
        } finally {
            try {
                operationLogService.record(request, response.getStatus(), failure);
            } catch (RuntimeException ex) {
                log.warn("operation_log_record_failed method={} uri={} message={}",
                        request.getMethod(), request.getRequestURI(), ex.getMessage());
            }
        }
    }
}
