package me.sonam.thumbnail.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.utils.StringUtils;

import java.time.Duration;

@Configuration
public class S3ClientConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(S3ClientConfiguration.class);

    @Bean
    public S3AsyncClient s3client(me.sonam.thumbnail.config.S3ClientConfigurationProperties s3props,
                                  AwsCredentialsProvider credentialsProvider) {
        SdkAsyncHttpClient httpClient = NettyNioAsyncHttpClient.builder()
                .writeTimeout(Duration.ZERO)
                .maxConcurrency(64)
                .build();
        S3Configuration serviceConfiguration = S3Configuration.builder()
                .checksumValidationEnabled(false)
                .chunkedEncodingEnabled(true)
                .build();
        S3AsyncClientBuilder b = S3AsyncClient.builder().httpClient(httpClient)
                .region(s3props.getRegion())
                .credentialsProvider(credentialsProvider)
                .serviceConfiguration(serviceConfiguration);

        if (s3props.getEndpoint() != null) {
            b = b.endpointOverride(s3props.getEndpoint());
        }
        return b.build();
    }

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider(me.sonam.thumbnail.config.S3ClientConfigurationProperties s3props) {

        if (StringUtils.isBlank(s3props.getAccessKeyId())) {
            // Return default provider
            LOG.warn("creating default credentials provider");
            return DefaultCredentialsProvider.create();
        }
        else {
            // Return custom credentials provider
            return () -> {
                LOG.info("returning custom credentials provider");
                AwsCredentials creds = AwsBasicCredentials.create(  s3props.getAccessKeyId(), s3props.getSecretAccessKey());
                return creds;
            };
        }
    }
}