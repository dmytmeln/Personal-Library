package org.example.library.pagination;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PaginationParams {
    private Integer page;
    private Integer size;
    private List<String> sort;
}