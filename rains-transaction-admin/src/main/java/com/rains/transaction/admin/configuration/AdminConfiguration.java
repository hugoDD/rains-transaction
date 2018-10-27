
package com.rains.transaction.admin.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rains.transaction.admin.interceptor.AuthInterceptor;
import com.rains.transaction.common.enums.SerializeProtocolEnum;
import com.rains.transaction.common.holder.ServiceBootstrap;
import com.rains.transaction.common.serializer.KryoSerializer;
import com.rains.transaction.common.serializer.ObjectSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

/**
 * <p>Description: .</p>
 *
 * @author hugoDD
 * @version 1.0
 * @date 2017/10/23 21:08
 * @since JDK 1.8
 */
@Configuration
@EnableConfigurationProperties(AdminTxProperties.class)
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class AdminConfiguration {


    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            /*   @Override
               public void addCorsMappings(CorsRegistry registry) {
                   registry.addMapping("/login*//*").allowedOrigins("*");
                registry.addMapping("/recover*//*").allowedOrigins("*");
                registry.addMapping("/tx*//*").allowedOrigins("*");

            }
*/
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new AuthInterceptor()).addPathPatterns("/**");
            }
        };
    }


    static class SerializerConfiguration {

        private final Environment env;

        @Autowired
        public SerializerConfiguration(Environment env) {
            this.env = env;
        }


        @Bean
        public ObjectSerializer objectSerializer(AdminTxProperties properties) {

            final SerializeProtocolEnum serializeProtocolEnum =
                    SerializeProtocolEnum.acquireSerializeProtocol(properties.getRecover().getSerializer());
            final ServiceLoader<ObjectSerializer> objectSerializers =
                    ServiceBootstrap.loadAll(ObjectSerializer.class);

            return StreamSupport.stream(objectSerializers.spliterator(), false)
                    .filter(objectSerializer ->
                            Objects.equals(objectSerializer.getScheme(),
                                    serializeProtocolEnum.getSerializeProtocol())).findFirst().orElse(new KryoSerializer());

        }

    }


    @Configuration
    static class RedisConfiguration {


        private final Environment env;

        @Autowired
        public RedisConfiguration(Environment env) {
            this.env = env;
        }


        @Bean
        public KeyGenerator keyGenerator() {
            return (target, method, params) -> {
                StringBuilder sb = new StringBuilder();
                sb.append(target.getClass().getName());
                sb.append(method.getName());
                for (Object obj : params) {
                    sb.append(obj.toString());
                }
                return sb.toString();
            };
        }


//        @Bean
//        @ConfigurationProperties(prefix = "tx.redis")
//        public JedisPoolConfig getRedisPoolConfig() {
//            return new JedisPoolConfig();
//        }

//        @Bean
//        @ConfigurationProperties(prefix = "tx.redis")
//        public JedisConnectionFactory getConnectionFactory() {
//
//            final Boolean cluster = env.getProperty("tx.redis.cluster", Boolean.class);
//            if (cluster) {
//                return new JedisConnectionFactory(getClusterConfiguration(),
//                        getRedisPoolConfig());
//            } else {
//                return new JedisConnectionFactory(getRedisPoolConfig());
//            }
//        }


        @Bean("redisTemplate")
        public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
            RedisTemplate redisTemplate = new StringRedisTemplate();
            redisTemplate.setConnectionFactory(redisConnectionFactory);

            Jackson2JsonRedisSerializer jackson2JsonRedisSerializer =
                    new Jackson2JsonRedisSerializer(Object.class);
            ObjectMapper om = new ObjectMapper();
            om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
            om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
            jackson2JsonRedisSerializer.setObjectMapper(om);
            redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
            redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
            redisTemplate.afterPropertiesSet();
            return redisTemplate;
        }
//
//        private RedisClusterConfiguration getClusterConfiguration() {
//            Map<String, Object> source = Maps.newHashMap();
//            source.put("spring.redis.cluster.nodes", env.getProperty("tx.redis.cluster.nodes"));
//            source.put("spring.redis.cluster.max-redirects", env.getProperty("tx.redis.cluster.redirects"));
//            return new RedisClusterConfiguration(new MapPropertySource("RedisClusterConfiguration", source));
//        }
    }
}
