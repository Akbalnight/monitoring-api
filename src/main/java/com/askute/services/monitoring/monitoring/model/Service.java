package com.askute.services.monitoring.monitoring.model;

public class Service {
    private Integer id;
    private String serviceName;
    private String serviceVersion;
    private Boolean serviceStatus;

    public Service(){}

    public Service(Integer id, String serviceName, String serviceVersion, Boolean serviceStatus){
        this.id = id;
        this.serviceName = serviceName;
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
}
