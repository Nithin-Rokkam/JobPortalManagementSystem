package com.capg.jobportal.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserInfoResponse {
    private Long id;
    private String name;
    private String email;
}
