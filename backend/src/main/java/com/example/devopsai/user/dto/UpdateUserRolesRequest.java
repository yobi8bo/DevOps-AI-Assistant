package com.example.devopsai.user.dto;

import java.util.List;

public record UpdateUserRolesRequest(List<Long> roleIds) {
}
