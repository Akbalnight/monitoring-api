package com.askute.services.monitoring.monitoring.model;

public class Service {
    private Integer id;
    private String serviceName;
    private String serviceUrl;
    private String serviceKey;
    private String serviceVersion;
    private Boolean serviceStatus;
    private String serverId;

    public Service(){}

    public Service(Integer id, String serviceName, String serviceUrl, String serviceKey, String serviceVersion, Boolean serviceStatus){
        this.id = id;
        this.serviceName = serviceName;
        this.serviceUrl = serviceUrl;
        this.serviceKey = serviceKey;
        this.serviceVersion = serviceVersion;
        this.serviceStatus = serviceStatus;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceStatus(Boolean serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public Boolean getServiceStatus() {
        return serviceStatus;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }
}
