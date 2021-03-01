package org.bc.auto;


import org.bc.auto.utils.SpringBeanUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "org.bc")
@MapperScan("org.bc.auto.dao")
public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private static ApplicationContext applicationContext;

    public static void main( String[] args ){
        applicationContext = SpringApplication.run(App.class, args);
        SpringBeanUtil.setApplicationContext(applicationContext);

    }
}
