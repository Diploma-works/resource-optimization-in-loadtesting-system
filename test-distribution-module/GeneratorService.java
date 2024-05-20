package com.ledokol.ledokolmain.service.discovery;

import com.ledokol.ledokolmain.db.model.test.Test;
import com.ledokol.ledokolmain.service.exception.GeneratorNotFoundException;
import com.ledokol.ledokolmain.service.exception.NoGeneratorsException;
import com.ledokol.ledokolmain.service.testrun.TestRunOptions;
import com.ledokol.ledokolmain.service.exception.StopTestOnGeneratorException;
import com.ledokol.ledokolmain.service.exception.TestNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class GeneratorService {

    private final ReactiveDiscoveryClient discoveryClient;

    @Value("${generator.endpoints.run}")
    private String generatorRunEndpoint;

    @Value("${generator.endpoints.stop}")
    private String generatorStopEndpoint;

    @Value("${generator.service.id}")
    private String generatorServiceId;

    @Autowired
    public GeneratorService(ReactiveDiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    public Flux<Generator> getAllRunningGenerators() {
        return discoveryClient.getInstances(generatorServiceId)
                .map(instance -> new Generator(instance.getUri().toString(), instance.getInstanceId()));
    }

    public Mono<Generator> getGeneratorById(String generatorId) {
        return discoveryClient
                .getInstances(generatorServiceId)
                .filter(generator -> generator.getInstanceId().equals(generatorId))
                .next()
                .switchIfEmpty(Mono.defer(() -> Mono.error(new GeneratorNotFoundException(generatorId))))
                .map(instance -> new Generator(instance.getUri().toString(), instance.getInstanceId()));
    }

    public Mono<GeneratorResponse> sendRunTestRequestToGenerator(Generator generator, Test test, TestRunOptions testRunOptions) {
        WebClient client = WebClient.create(generator.getUrl() + generatorRunEndpoint);
        Map<String, Object> request = new HashMap<>();
        request.put("test", test);
        request.put("options", testRunOptions);
        return client.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchangeToMono(response -> response.bodyToMono(String.class)
                        .map(responseMessage -> new GeneratorResponse(response.statusCode(), responseMessage)));
    }

    public Mono<GeneratorResponse> sendStopTestRequestToGenerator(Generator generator, String testRunId) {
        WebClient client = WebClient.create(String.format("%s/%s%s", generator.getUrl(), testRunId, generatorStopEndpoint));
        return client.post()
                .contentType(MediaType.APPLICATION_JSON)
                .exchangeToMono(response ->  response.bodyToMono(String.class)
                        .map(responseMessage -> new GeneratorResponse(response.statusCode(), responseMessage)))
                .onErrorResume(throwable -> Mono.error(new StopTestOnGeneratorException(testRunId, generator)));
    }
}