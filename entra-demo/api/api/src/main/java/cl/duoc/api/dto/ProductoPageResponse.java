package cl.duoc.api.dto;

import java.util.List;

public record ProductoPageResponse(
    List<ProductoResponse> content,
    long totalElements,
    int totalPages,
    int size,
    int number,
    int numberOfElements,
    boolean first,
    boolean last,
    boolean empty
) {
}