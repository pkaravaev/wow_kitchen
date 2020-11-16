package com.foodtech.back.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@Profile({"prod", "dev"})
public class AsyncConfig implements AsyncConfigurer {

    @Override
    @Bean(name = "asyncMethodExecutor")
    public ThreadPoolTaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(Integer.MAX_VALUE);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "statusCheckersExecutor")
    public ThreadPoolTaskExecutor getStatusCheckerPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(Integer.MAX_VALUE);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("checker-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "paymentCompletionExecutor")
    public ThreadPoolTaskExecutor getPaymentCompletionPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(Integer.MAX_VALUE);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("payment-");
        executor.initialize();
        return executor;
    }

//    @Bean(name = "paymentCompletionExecutor")
//    public ThreadPoolTaskExecutor getPaymentCompletionPool() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(20);
//        executor.setMaxPoolSize(Integer.MAX_VALUE);
//        executor.setQueueCapacity(20);
//        executor.setThreadNamePrefix("payment-");
//        executor.initialize();
//        return executor;
//    }
//
//    @Bean(name = "applicationEventMulticaster")
//    public ApplicationEventMulticaster simpleApplicationEventMulticaster() {
//        SimpleApplicationEventMulticaster eventMulticaster =
//                new SimpleApplicationEventMulticaster();
//
//        eventMulticaster.setTaskExecutor(getStatusCheckerPool());
//        return eventMulticaster;
//    }

}
