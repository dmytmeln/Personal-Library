package org.example.library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import smile.nlp.tokenizer.SimpleTokenizer;

@Configuration
public class NlpConfig {

    @Bean
    public SimpleTokenizer simpleTokenizer() {
        return new SimpleTokenizer();
    }
}
