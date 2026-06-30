package com.example.devopsai.user.dto;

public record UserQuery(String keyword, Integer status, long pageNum, long pageSize) {
}
