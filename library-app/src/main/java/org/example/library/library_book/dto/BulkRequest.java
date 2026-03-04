package org.example.library.library_book.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkRequest {

    @NotEmpty(message = "{validation.bulk.not_empty}")
    @Size(max = 50, message = "{validation.bulk.max_items}")
    private List<Integer> ids;

}
