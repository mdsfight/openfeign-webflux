package com.zhl.cloud.openfeign.webflux.registrar;

import com.zhl.cloud.openfeign.webflux.annotation.WebfluxClient;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * @author zhl
 * @date 2025/5/6 10:04:27
 */
public class WebClientFactory implements FactoryBean<Object>, ApplicationContextAware, InitializingBean , EnvironmentAware {

    private final Class<?> interFace;

    private ApplicationContext applicationContext;

    private Object proxyClient;

    private final WebfluxClient webfluxClient;

    private Environment environment;

    public WebClientFactory(Class<?> interFace,WebfluxClient webfluxClient) {
        this.interFace = interFace;
        this.webfluxClient = webfluxClient;
    }

    @Override
    public Object getObject() {
        return this.proxyClient;
    }

    @Override
    public Class<?> getObjectType() {
        return interFace;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        String url = environment.resolvePlaceholders(webfluxClient.url());
        if(url.startsWith("$")){
            throw new IllegalArgumentException("Could not resolve placeholder '" + webfluxClient.url().replace("$","").replace("{","").replace("}","") + "'");
        }
        String targetUrl = url.isEmpty() ? this.webfluxClient.name() : url;
        Map<String, ExchangeFilterFunction> exchangeFilterFunctionMap = applicationContext.getBeansOfType(ExchangeFilterFunction.class);
        WebClient webClient = WebClient.builder()
                .filters(filters -> {
                    if(url.isEmpty()){
                        filters.add(exchangeFilterFunctionMap.get("com.bestune.cloud.openfeign.webflux.filter.LoadBalancerFilterFunction"));
                    }
                    exchangeFilterFunctionMap.forEach((key, value) -> {
                        if("com.bestune.cloud.openfeign.webflux.filter.LoadBalancerFilterFunction".equals(key)){
                            return;
                        }
                        filters.add(value);
                    });
                })
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) webfluxClient.connectTimeout())  // 连接超时（毫秒）
                        .responseTimeout(Duration.ofMillis(webfluxClient.readTimeout()))             // 响应超时
                        .doOnConnected(conn ->
                                conn.addHandlerLast(new ReadTimeoutHandler(webfluxClient.readTimeout(), TimeUnit.MILLISECONDS))   // 读取超时（秒）
                        )))
                .baseUrl(targetUrl.startsWith("http") ? targetUrl : "http://" + targetUrl)
                .build();
        this.proxyClient = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient))
                .build()
                .createClient(interFace);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
