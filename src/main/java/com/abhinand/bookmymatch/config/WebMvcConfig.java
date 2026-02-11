package com.abhinand.bookmymatch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Get absolute path to uploads directory
        String uploadPath = Paths.get("src/main/resources/static/uploads")
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();

        // Serve uploaded files
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath, "classpath:/static/uploads/")
                .setCachePeriod(3600)
                .resourceChain(true);


        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCachePeriod(3600);

        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCachePeriod(3600);

        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCachePeriod(3600);
    }
}