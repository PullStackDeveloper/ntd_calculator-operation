package com.pullstackdeveloper.ntdcalculatoroperation.config;

import com.pullstackdeveloper.ntdcalculatoroperation.filter.CustomRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private CustomRequestFilter customRequestFilter;

    @Bean
    public FilterRegistrationBean<CustomRequestFilter> customFilter() {
        FilterRegistrationBean<CustomRequestFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(customRequestFilter);
        registrationBean.addUrlPatterns("/*"); // Apply to all URL patterns
        return registrationBean;
    }
}