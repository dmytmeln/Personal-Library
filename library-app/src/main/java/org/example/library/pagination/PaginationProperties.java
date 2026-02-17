package org.example.library.pagination;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.pagination")
@Getter
@Setter
@NoArgsConstructor
public class PaginationProperties {
    private int defaultPageSize = 20;
    private int maxPageSize = 100;
    private int minPageSize = 1;
}
