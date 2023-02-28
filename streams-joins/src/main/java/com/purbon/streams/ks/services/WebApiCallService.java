package com.purbon.streams.ks.services;

import com.purbon.streams.ks.model.Quote;
import org.springframework.stereotype.Service;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Service
public class WebApiCallService {

    private final WebClient webClient;

    public WebApiCallService(WebClient.Builder webClientBuilder) {
        var httpClient = HttpClient.create().resolver(DefaultAddressResolverGroup.INSTANCE);
        this.webClient = webClientBuilder
                .baseUrl("http://localhost:8080")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Retryable(maxAttempts = 4, value = java.net.ConnectException.class, backoff = @Backoff(delay = 3000, multiplier = 2))
    public Mono<Quote> quoteRestCall() {
        return webClient.get()
                .uri("/quote")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Quote.class);
    }
}
