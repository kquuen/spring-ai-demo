package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.codec.CodecConfigurer;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OpenAiConfig {

    // 修复普通接口（非流式）
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder()
                .messageConverters(converters ->
                        converters.stream()
                                .filter(c -> c instanceof MappingJackson2HttpMessageConverter)
                                .map(c -> (MappingJackson2HttpMessageConverter) c)
                                .forEach(c -> {
                                    List<MediaType> types = new ArrayList<>(c.getSupportedMediaTypes());
                                    types.add(MediaType.APPLICATION_OCTET_STREAM);
                                    c.setSupportedMediaTypes(types);
                                })
                );
    }

    // 修复流式接口（SSE）
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .codecs(configurer -> {
                    CodecConfigurer.DefaultCodecs defaults = configurer.defaultCodecs();
                    defaults.maxInMemorySize(5 * 1024 * 1024);
                    // 让 WebClient 也能处理 octet-stream 类型的流式响应
                    configurer.customCodecs().register(
                            new org.springframework.http.codec.json.Jackson2JsonDecoder(
                                    new com.fasterxml.jackson.databind.ObjectMapper(),
                                    MediaType.APPLICATION_OCTET_STREAM,
                                    MediaType.APPLICATION_JSON
                            )
                    );
                });
    }
}