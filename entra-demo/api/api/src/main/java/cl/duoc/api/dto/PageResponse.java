
package cl.duoc.api.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PageResponse<T>(
    List<T> content,
    int number,
    int size,
    int totalPages,
    long totalElements,
    int numberOfElements,
    boolean first,
    boolean last,
    boolean empty
) {}
