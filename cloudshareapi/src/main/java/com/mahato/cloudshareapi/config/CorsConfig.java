package com.mahato.cloudshareapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Explicit CORS configuration for production deployment.
 * Allows Vercel frontend to communicate with Render backend.
 */
@Configuration
public class CorsConfig {

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Ensure exact frontend origin is allowed even if APP_FRONTEND_URL has a trailing slash.
        String normalizedFrontendOrigin = normalizeOrigin(frontendUrl);
        List<String> allowedOrigins = Arrays.asList(
                normalizedFrontendOrigin,
                "http://localhost:5173"
        );
        config.setAllowedOrigins(allowedOrigins);

        // Optional support for Vercel preview deployments while keeping credentials enabled.
        config.setAllowedOriginPatterns(Arrays.asList(
                "https://*.vercel.app"
        ));

        // Allow all HTTP methods
        config.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS",
                "HEAD"
        ));

        // Allow necessary headers
        config.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Access-Control-Allow-Credentials"
        ));

        // Allow credentials (cookies, authorization headers)
        config.setAllowCredentials(true);

        // Cache preflight requests for 1 hour
        config.setMaxAge(3600L);

        // Expose response headers if needed
        config.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type"
        ));

        // Register configuration for all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

        private String normalizeOrigin(String origin) {
                if (origin == null) {
                        return "http://localhost:5173";
                }

                String trimmed = origin.trim();
                if (trimmed.endsWith("/")) {
                        return trimmed.substring(0, trimmed.length() - 1);
                }

                return trimmed;
        }
}
