package com.zhl.cloud.openfeign.webflux.filter;

import com.zhl.cloud.openfeign.webflux.exception.WebfluxClientException;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * @author zhl
 * @date 2025/5/6 10:12:45
 */
@Slf4j
public class LoadBalancerFilterFunction implements ExchangeFilterFunction {

    @Resource
    private LoadBalancerClient loadBalancerClient;

    @Override
    @Nonnull
    public Mono<ClientResponse> filter(ClientRequest request, @Nonnull ExchangeFunction next) {
        URI originalUrl = request.url();
        String serviceName = originalUrl.getHost();
        return Mono.defer(() -> Mono.justOrEmpty(loadBalancerClient.choose(serviceName)))
                .switchIfEmpty(Mono.error(new WebfluxClientException(
                        "No instances available for service: " + serviceName, HttpStatus.SERVICE_UNAVAILABLE)))
                .flatMap(instance -> {
                    URI reconstructedUrl = reconstructUri(instance, originalUrl);
                    ClientRequest newRequest = ClientRequest.from(request)
                            .url(reconstructedUrl)
                            .build();
                    return next.exchange(newRequest);
                });
    }

    private static URI reconstructUri(ServiceInstance instance, URI originalUri) {
        String path = originalUri.getPath();
        if(originalUri.getQuery() != null && !originalUri.getQuery().isEmpty()) {
            path += "?" + originalUri.getQuery();
        }
        return URI.create(String.format("%s://%s:%s%s",
                originalUri.getScheme(),
                instance.getHost(),
                instance.getPort(),
                path));
    }
}
