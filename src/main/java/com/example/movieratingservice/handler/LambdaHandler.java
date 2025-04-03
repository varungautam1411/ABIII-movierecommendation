package com.example.movieratingservice.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.example.movieratingservice.model.Rating;
import org.springframework.cloud.function.adapter.aws.SpringBootRequestHandler;

@SuppressWarnings({ "deprecation", "unused" })
public class LambdaHandler extends SpringBootRequestHandler<Rating, String> {
}

    
