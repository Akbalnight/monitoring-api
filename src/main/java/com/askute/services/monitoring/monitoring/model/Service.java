package com.askute.services.monitoring.monitoring.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Service {
    private Integer id;
    private String serviceName;
    private String serviceUrl;
    private String serviceKey;
    private String serviceVersion;
    private Boolean serviceStatus;
    private OffsetDateTime updateTime;
    private String serverName;
    private String serviceVersionPath;
}
