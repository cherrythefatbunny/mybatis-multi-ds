package com.github.cherrythefatbuny;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@EnableConfigurationProperties(MultiDataSourceProperties.class)
public class MutilDataSourceAutoConfiguration {
    private final MybatisProperties mybatisProperties;
    private final Interceptor[] interceptors;
    private final ResourceLoader resourceLoader;
    private final DatabaseIdProvider databaseIdProvider;
    private final List<ConfigurationCustomizer> configurationCustomizers;
    @Autowired
    MultiDataSourceProperties multiDataSourceProperties;

    /**
     * the same as {@link MybatisAutoConfiguration#MybatisAutoConfiguration(MybatisProperties, ObjectProvider, ResourceLoader, ObjectProvider, ObjectProvider)},
     * parameters are used to initialize SqlSessionFactory
     * */
    public MutilDataSourceAutoConfiguration(MybatisProperties properties,
                                    ObjectProvider<Interceptor[]> interceptorsProvider,
                                    ResourceLoader resourceLoader,
                                    ObjectProvider<DatabaseIdProvider> databaseIdProvider,
                                    ObjectProvider<List<ConfigurationCustomizer>> configurationCustomizersProvider) {
        this.mybatisProperties = properties;
        this.interceptors = interceptorsProvider.getIfAvailable();
        this.resourceLoader = resourceLoader;
        this.databaseIdProvider = databaseIdProvider.getIfAvailable();
        this.configurationCustomizers = configurationCustomizersProvider.getIfAvailable();
    }


    protected DataSource createDataSource(DataSourceProperties properties, Class<? extends DataSource> type) {
        return properties.initializeDataSourceBuilder().type(type).build();
    }

    /**
     * the same as {@link MybatisAutoConfiguration#sqlSessionFactory(DataSource)},create SqlSessionFactory with specified data source
     * */
    protected SqlSessionFactory getSqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setVfs(SpringBootVFS.class);
        if (StringUtils.hasText(mybatisProperties.getConfigLocation())) {
            factory.setConfigLocation(this.resourceLoader.getResource(this.mybatisProperties.getConfigLocation()));
        }
        org.apache.ibatis.session.Configuration configuration = this.mybatisProperties.getConfiguration();
        if (configuration == null && !StringUtils.hasText(this.mybatisProperties.getConfigLocation())) {
            configuration = new org.apache.ibatis.session.Configuration();
        }
        if (configuration != null && !CollectionUtils.isEmpty(this.configurationCustomizers)) {
            for (ConfigurationCustomizer customizer : this.configurationCustomizers) {
                customizer.customize(configuration);
            }
        }
        factory.setConfiguration(configuration);
        if (this.mybatisProperties.getConfigurationProperties() != null) {
            factory.setConfigurationProperties(this.mybatisProperties.getConfigurationProperties());
        }
        if (!ObjectUtils.isEmpty(this.interceptors)) {
            factory.setPlugins(this.interceptors);
        }
        if (this.databaseIdProvider != null) {
            factory.setDatabaseIdProvider(this.databaseIdProvider);
        }
        if (StringUtils.hasLength(this.mybatisProperties.getTypeAliasesPackage())) {
            factory.setTypeAliasesPackage(this.mybatisProperties.getTypeAliasesPackage());
        }
        if (StringUtils.hasLength(this.mybatisProperties.getTypeHandlersPackage())) {
            factory.setTypeHandlersPackage(this.mybatisProperties.getTypeHandlersPackage());
        }
        if (!ObjectUtils.isEmpty(this.mybatisProperties.resolveMapperLocations())) {
            factory.setMapperLocations(this.mybatisProperties.resolveMapperLocations());
        }

        return factory.getObject();
    }
    /**
     * the same as {@link MybatisAutoConfiguration#sqlSessionTemplate(SqlSessionFactory)},
     * create SqlSessionFactory with specified data source
     * */
    protected SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        ExecutorType executorType = this.mybatisProperties.getExecutorType();
        if (executorType != null) {
            return new SqlSessionTemplate(sqlSessionFactory, executorType);
        } else {
            return new SqlSessionTemplate(sqlSessionFactory);
        }
    }

    /**
     * method to replace default SqlSessionTemplate of MybatisFactoryBean with the specified one
     * which is supported by multiDataSourceProperties
     * */
    @Bean
    BeanPostProcessor initMultiDataSourceMapperFactoryBean() throws Exception {
        Map<String, SqlSessionTemplate> sqlSessionTemplates = new HashMap<>();
        for(Map.Entry<String, DataSourceProperties> entry:multiDataSourceProperties.getDatasources().entrySet()) {
            Class<? extends DataSource> dataSourceType = entry.getValue().getType();
            DataSource dataSource = createDataSource(entry.getValue(), dataSourceType);
            SqlSessionFactory sqlSessionFactory = getSqlSessionFactory(dataSource);
            SqlSessionTemplate sqlSessionTemplate = sqlSessionTemplate(sqlSessionFactory);
            sqlSessionTemplates.put(entry.getKey(), sqlSessionTemplate);
        }
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                if(bean.getClass() == MapperFactoryBean.class) {
                    MapperFactoryBean mapperFactoryBean = (MapperFactoryBean)bean;
                    if(mapperFactoryBean.getMapperInterface().isAnnotationPresent(Ds.class)) {
                        Ds datasource = (Ds) mapperFactoryBean.getMapperInterface().getDeclaredAnnotation(Ds.class);
                        String name = datasource.value();
                        SqlSessionTemplate sqlSessionTemplate = sqlSessionTemplates.get(name);
                        if(null == sqlSessionTemplate) {
                            log.warn(String.format("datasource '%s' for mapper '%s' not found",
                                    name, mapperFactoryBean.getMapperInterface().getName()));
                        } else {
                            mapperFactoryBean.setSqlSessionTemplate(sqlSessionTemplate);
                        }
                    }
                }
                return bean;
            }
        };
    }
}
