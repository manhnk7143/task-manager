package com.ctel.dbaas.config;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class RedisConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(EnvConfig.REDIS_STANDALONE_HOST);
        configuration.setPort(EnvConfig.REDIS_STANDALONE_PORT);
        configuration.setPassword(EnvConfig.REDIS_STANDALONE_PASSWORD);
        configuration.setDatabase(EnvConfig.REDIS_STANDALONE_DATABASE);

//        RedisProperties redisProperties = new RedisProperties();
//        redisProperties.setDatabase(Integer.parseInt(System.getenv("REDIS_DB")));
//        redisProperties.setPort(Integer.parseInt(System.getenv("REDIS_PORT")));
//        redisProperties.setPassword(System.getenv("REDIS_PASSWORD"));
//        RedisProperties.Sentinel sentinel = new RedisProperties.Sentinel();
//        sentinel.setMaster(System.getenv("REDIS_MASTER_NAME"));
//        sentinel.setNodes(List.of(System.getenv("REDIS_HOSTS").split(",")));
//        sentinel.setPassword(System.getenv("REDIS_SENTINEL_PASSWORD"));
//        redisProperties.setSentinel(sentinel);

//        RedisSentinelConfiguration configuration = new RedisSentinelConfiguration();
//        configuration.setDatabase(Integer.parseInt(System.getenv("REDIS_DB")));
//        configuration.setPassword(System.getenv("REDIS_PASSWORD"));
//        configuration.master(System.getenv("REDIS_MASTER_NAME"));
//
//        configuration.setSentinelPassword(System.getenv("REDIS_SENTINEL_PASSWORD"));
//        String[] redisNodes = System.getenv("REDIS_HOSTS").split(",");
//        for (String redisNode : redisNodes) {
//            String host = redisNode.split(":")[0];
//            int port = Integer.parseInt(redisNode.split(":")[1]);
//            RedisNode rdNode = RedisNode.newRedisNode()
//                    .listeningAt(host, port)
//                    .build();
//            configuration.addSentinel(rdNode);
//        }

        return new LettuceConnectionFactory(configuration);
    }

    @Bean(name = "redisTokenPortalV2")
    public RedisTemplate<String, String> redisTokenPortalV2(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

}
