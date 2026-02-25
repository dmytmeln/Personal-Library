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
public class BulkLibraryBookRequest {

    @NotEmpty(message = "List of IDs cannot be empty")
    @Size(max = 50, message = "Cannot process more than 50 items at once")
    private List<Integer> ids;

}
