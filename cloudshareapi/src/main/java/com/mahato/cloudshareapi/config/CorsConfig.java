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

        // Allow specific origins (Vercel frontend + development URLs)
        config.setAllowedOrigins(Arrays.asList(
                "https://file-sharing-app-inky.vercel.app",      // Production Vercel URL
                "http://localhost:5173",                          // Local Vite dev
                "http://localhost:3000",                          // Local fallback
                "http://127.0.0.1:5173",                          // Localhost variant
                "http://127.0.0.1:3000"                           // Localhost variant
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
}
