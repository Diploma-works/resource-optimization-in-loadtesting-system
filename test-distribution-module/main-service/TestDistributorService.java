package com.ledokol.ledokolmain.service;

import com.ledokol.ledokolmain.db.model.test.RunOnGenerator;
import com.ledokol.ledokolmain.db.model.test.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class TestDistributorService {

    private final ReactiveDiscoveryClient discoveryClient;

    @Value("${generator.endpoints.run}")
    private String getTestPlanEndpoint;

    @Value("${test-distributor.service.id}")
    private String distributorServiceId;

    @Autowired
    public TestDistributorService(ReactiveDiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }


    public Flux<RunOnGenerator> getTestPlanForTest(Test test) {
        WebClient client = WebClient.create(String.format("%s/%s%s", distributorServiceId.getUrl(), test, getTestPlanEndpoint));
        return client.post()
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(RunOnGenerator.class)
                .onErrorResume(throwable -> {
                    return Mono.error(new CreatingTestPlanException(test.getId(), throwable));
                });
    }

}