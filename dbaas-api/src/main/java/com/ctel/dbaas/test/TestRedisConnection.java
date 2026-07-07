package com.ctel.dbaas.test;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/test-redis-client")
public class TestRedisConnection {

    @PostMapping("/standalone")
    public Object standalone(@RequestBody TestStandalone req) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(req.getHost());
        configuration.setPort(req.getPort());
        configuration.setPassword(req.getPassword());
        configuration.setDatabase(req.getDatabase());

        template.setConnectionFactory(new LettuceConnectionFactory(configuration));
        template.opsForValue().set("ManhNK-key-standalone", "ManhNK-value-standalone", 10, TimeUnit.SECONDS);
        return Collections.singletonMap("data", template.opsForValue().get("ManhNK-key-standalone"));
    }

    @PostMapping("/sentinel-1")
    public Object sentinel1(@RequestBody TestSentinel req) {
        RedisSentinelConfiguration configuration = new RedisSentinelConfiguration();
        configuration.setDatabase(req.getDatabase());
        configuration.setPassword(req.getPassword());
        configuration.master(req.getMasterName());

        configuration.setSentinelPassword(req.getSentinelPassword());
        String[] redisNodes = req.getHosts().split(",");
        for (String redisNode : redisNodes) {
            String host = redisNode.split(":")[0];
            int port = Integer.parseInt(redisNode.split(":")[1]);
            RedisNode rdNode = RedisNode.newRedisNode()
                    .listeningAt(host, port)
                    .build();
            configuration.addSentinel(rdNode);
        }

        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(new LettuceConnectionFactory(configuration));
        template.opsForValue().set("ManhNK-key-sentinel", "ManhNK-value-sentinel", 10, TimeUnit.SECONDS);
        return Collections.singletonMap("data", template.opsForValue().get("ManhNK-key-sentinel"));
    }

//    @PostMapping("/sentinel-2")
//    public Object sentinel2(@RequestBody TestSentinel req) {
//        RedisProperties redisProperties = new RedisProperties();
//        redisProperties.setDatabase(Integer.parseInt(System.getenv("REDIS_DB")));
//        redisProperties.setPort(Integer.parseInt(System.getenv("REDIS_PORT")));
//        redisProperties.setPassword(System.getenv("REDIS_PASSWORD"));
//        RedisProperties.Sentinel sentinel = new RedisProperties.Sentinel();
//        sentinel.setMaster(System.getenv("REDIS_MASTER_NAME"));
//        sentinel.setNodes(List.of(System.getenv("REDIS_HOSTS").split(",")));
//        sentinel.setPassword(System.getenv("REDIS_SENTINEL_PASSWORD"));
//        redisProperties.setSentinel(sentinel);
//
//        RedisSentinelConfiguration configuration = new RedisSentinelConfiguration();
//
//        RedisTemplate<String, String> template = new RedisTemplate<>();
//        template.setConnectionFactory(new LettuceConnectionFactory(configuration));
//        template.opsForValue().set("ManhNK-key-sentinel", "ManhNK-value-sentinel", 10, TimeUnit.SECONDS);
//        return template.opsForValue().get("ManhNK-key-sentinel");
//    }

    @Data
    public static class TestStandalone {
        private String host;
        private int port;
        private String password;
        private int database;
    }

    @Data
    public static class TestSentinel {
        private int database;
        private String password;
        private String masterName;
        private String sentinelPassword;
        private String hosts;
    }

}
