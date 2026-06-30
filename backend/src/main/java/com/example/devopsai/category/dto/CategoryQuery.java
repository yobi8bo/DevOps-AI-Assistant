package com.example.devopsai.category.dto;

public record CategoryQuery(String keyword, Integer status, long pageNum, long pageSize) {
}
