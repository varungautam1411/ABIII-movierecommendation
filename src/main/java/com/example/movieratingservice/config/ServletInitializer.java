package com.example.movieratingservice.config;

import com.amazonaws.serverless.proxy.spring.SpringLambdaContainerHandler;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import com.example.movieratingservice.MovieRatingServiceApplication;

@Configuration
public class ServletInitializer extends SpringBootServletInitializer {
    
    @Override
    protected WebApplicationContext createRootApplicationContext() {
        WebApplicationContext context = super.createRootApplicationContext();
        // Add Lambda response flush listener
        DispatcherServlet dispatcherServlet = context.getBean(DispatcherServlet.class);
        dispatcherServlet.addListener(new SpringLambdaContainerHandler.LambdaFlushResponseListener());
        return context;
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[] { MovieRatingServiceApplication.class };
    }
}
