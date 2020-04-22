package de.daroge.docdemo;

import com.github.davidmoten.rx.jdbc.Database;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.eviction.EvictionType;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Time;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@ComponentScan
@Configuration
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
public class NoteApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoteApplication.class,args);
    }

    @Autowired
    private Environment env;

    private EmbeddedCacheManager manager;

    @Bean
    @Primary
    public DataSource dataSource(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(Objects.requireNonNull(env.getProperty("spring.datasource.driver-class-name")));
        dataSource.setUrl(env.getProperty("spring.datasource.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.password"));
        return dataSource;
    }

    @Bean
    public EmbeddedCacheManager cacheManager() {
        ConfigurationBuilder config = new ConfigurationBuilder();
        config
                .expiration().lifespan(1, TimeUnit.HOURS)
                .expiration().maxIdle(30,TimeUnit.MINUTES)
                .simpleCache(true)
                .memory().size(5000);
        this.manager = new DefaultCacheManager();
        manager.defineConfiguration("noteCache",config.build());
        return manager;
    }

    @PreDestroy
    public void releaseCache() throws IOException {
        manager.close();
    }

    @Bean
    public Database database(){
        return Database.builder()
                .url(env.getProperty("spring.datasource.url"))
                .username(env.getProperty("spring.datasource.username"))
                .password(env.getProperty("spring.datasource.password"))
                .pool(Runtime.getRuntime().availableProcessors() * 2,Runtime.getRuntime().availableProcessors() * 3)
                .build();
    }
}
