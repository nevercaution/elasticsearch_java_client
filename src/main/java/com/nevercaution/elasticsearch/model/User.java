package com.nevercaution.elasticsearch.model;

import lombok.Data;

import java.util.Date;

@Data
public class User {
    private String user;
    private Date postDate;
    private String message;
}
