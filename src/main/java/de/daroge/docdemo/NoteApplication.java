package de.daroge.docdemo;

import com.github.davidmoten.rx.jdbc.ConnectionProvider;
import com.github.davidmoten.rx.jdbc.ConnectionProviderFromUrl;
import com.github.davidmoten.rx.jdbc.Database;
import org.infinispan.manager.DefaultCacheManager;
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
import java.util.Objects;

@ComponentScan
@Configuration
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
public class NoteApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoteApplication.class,args);
    }

    @Autowired
    private Environment env;

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
    public DefaultCacheManager cacheManager() throws IOException {
        return new DefaultCacheManager("config.xml");
    }

    @PreDestroy
    public void stopCache() throws IOException {
        cacheManager().stop();
    }

    @Bean
    public Database database(){
        ConnectionProvider connectionProvider = new ConnectionProviderFromUrl(env.getProperty("spring.datasource.url"),
                env.getProperty("spring.datasource.username"),env.getProperty("spring.datasource.password"));
        return Database.from(connectionProvider);
    }
}
