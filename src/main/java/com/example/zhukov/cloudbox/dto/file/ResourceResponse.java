package com.example.zhukov.cloudbox.dto.file;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResourceResponse {
    private String path;
    private String name;
    private Long size;
    private String type;
}
