package Bookings.DTO;

import lombok.Data;

@Data
public class AddUserToProjectRequest {
    private Long userId;
    private Long projectId;
}