package io.enfuse.java.ml.mlapp.configuration;

import io.enfuse.java.ml.mlapp.configuration.properties.MlAppConfigurationProperties;
import io.enfuse.java.ml.mlapp.domain.NewsFeatures;
import io.enfuse.java.ml.mlapp.modelserve.JavaModelServer;
import ml.combust.mleap.avro.SchemaConverter;
import ml.combust.mleap.core.types.StructType;
import ml.combust.mleap.runtime.MleapContext;
import ml.combust.mleap.runtime.javadsl.ContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MlAppConfiguration {

    @Bean
    public MleapContext mleapContext() {
        return new ContextBuilder().createMleapContext();
    }

    @Bean
    public JavaModelServer javaModelServer(MlAppConfigurationProperties mlAppConfigurationProperties) {
        StructType structType = SchemaConverter.avroToMleap(new NewsFeatures().getSchema(), mleapContext());
        return new JavaModelServer(mlAppConfigurationProperties.getModelPath(), structType, mleapContext());
    }
}
