package Bookings.DTO;

import lombok.Data;

@Data
public class CreateProjectRequest {
    private String name;
    private String description;
}