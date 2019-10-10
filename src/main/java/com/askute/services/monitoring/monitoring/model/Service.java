package com.askute.services.monitoring.monitoring.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Service {
    private UUID id;
    private Integer code;
    private String serviceName;
    private String serviceUrl;
    private String serviceKey;
    private String serviceVersion;
    private Boolean serviceStatus;
    private OffsetDateTime updateTime;
    private String serverName;
    private String serviceVersionPath;
}
