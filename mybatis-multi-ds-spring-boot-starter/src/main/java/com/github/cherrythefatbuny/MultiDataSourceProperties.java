package com.github.cherrythefatbuny;

import lombok.Data;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.github.cherrythefatbuny.MultiDataSourceProperties.PREFIX;

/**
 * @author cherry
 */
@Data
@ConfigurationProperties(prefix = PREFIX)
public class MultiDataSourceProperties {
    public static final String PREFIX = "spring";
    /**
     * 每一个数据源
     */
    private Map<String, DataSourceProperties> datasources = new LinkedHashMap<>();
}
