package com.mahato.cloudshareapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Explicit CORS configuration for production deployment.
 * Allows Vercel frontend to communicate with Render backend.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Use patterns to support dynamic Vercel preview URLs while allowing credentials
        config.setAllowedOriginPatterns(Arrays.asList("*"));

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
}
