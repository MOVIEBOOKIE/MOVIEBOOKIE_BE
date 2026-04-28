package project.luckybooky.global.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class S3StorageConfig {
    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.endpoint:}")
    private String endPoint;

    @Value("${aws.s3.access-key:}")
    private String accessKey;

    @Value("${aws.s3.secret-key:}")
    private String secretKey;

    @Bean
    public AmazonS3 amazonS3() {
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();

        if (StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey)) {
            BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
            builder.withCredentials(new AWSStaticCredentialsProvider(credentials));
        } else {
            builder.withCredentials(DefaultAWSCredentialsProviderChain.getInstance());
        }

        if (StringUtils.hasText(endPoint)) {
            builder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint, region))
                    .withPathStyleAccessEnabled(true);
        } else {
            builder.withRegion(region);
        }

        return builder.build();
    }
}
