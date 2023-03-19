package com.srb.core.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@MapperScan("com.srb.core.mapper")
@EnableTransactionManagement  //事务处理
public class MyBatisConfig {

    @Bean
    public MybatisPlusInterceptor myBatisPlusInterceptor(){
        MybatisPlusInterceptor intercept=new MybatisPlusInterceptor();
        intercept.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return intercept;
    }

}
