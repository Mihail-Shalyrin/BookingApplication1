package Bookings.DTO;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken;
}