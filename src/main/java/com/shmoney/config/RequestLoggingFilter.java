package com.shmoney.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {
    
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper res = new ContentCachingResponseWrapper(response);
        
        String method = req.getMethod();
        String uri = req.getRequestURI();
        String origin = req.getHeader("Origin");
        String host = req.getHeader("Host");
        String ua = req.getHeader("User-Agent");
        
        log.info("--> {} {} (Origin={}, Host={}, UA={})", method, uri, origin, host, ua);
        
        try {
            filterChain.doFilter(req, res);
        } finally {
            String reqBody = bodySnippet(req.getContentAsByteArray(), 512);
            String resBody = bodySnippet(res.getContentAsByteArray(), 512);
            log.info("<-- {} {} status={} reqBody='{}' resBody='{}'", method, uri, res.getStatus(), reqBody, resBody);
            res.copyBodyToResponse();
        }
    }
    
    private String bodySnippet(byte[] bytes, int max) {
        if (bytes == null || bytes.length == 0) return "";
        String s = new String(bytes, StandardCharsets.UTF_8);
        if (s.length() > max) return s.substring(0, max) + "...";
        return s;
    }
}

