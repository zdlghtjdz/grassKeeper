package com.github.soh.autocommit.grassKeeper.batch.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
@Getter @Setter
public class BatchProperties {
    private String queueDir;
}
