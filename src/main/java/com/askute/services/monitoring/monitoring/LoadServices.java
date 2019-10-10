package com.askute.services.monitoring.monitoring;

import com.askute.services.monitoring.monitoring.dao.MonitoringDao;
import com.askute.services.monitoring.monitoring.model.DeferredQuote;
import com.askute.services.monitoring.monitoring.model.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Log4j2
@Component
public class LoadServices {

    @Autowired
    MonitoringDao monitoringDao;

    @Value("${config}")
    private String CONFIG_PATH;

    private String SERVER_NAME;

    /**
     * Очередь клиентов на получение статусов сервисов
     */
    private final Queue<DeferredQuote> responseBodyQueue = new ConcurrentLinkedQueue<>();

    private final ObjectMapper objectMapper;

//    private List<JsonService> jsonServices;
    private List<Service> services;
    private RestTemplate rest;
    private HttpHeaders headers;
    private HttpStatus status;

    @Autowired
    public LoadServices(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @PostConstruct
    private void init() {
        this.rest = new RestTemplate();
        services = monitoringDao.selectByServerNameMgServices(SERVER_NAME);
    }

    @Scheduled(fixedRate = 10000)
    private void checkServices(){
        checkOwnIp();
        services = monitoringDao.selectByServerNameMgServices(SERVER_NAME);
//        log.info("Count check services: [{}]", services.size());
        for (int i = 0; i < services.size(); i++){
            checkService(services.get(i), false);
        }

        List<Service> Ls = monitoringDao.selectAllMgServices();

        for (DeferredQuote result : responseBodyQueue) {
            result.setResult(Ls);
            responseBodyQueue.remove(result);
        }

    }

    private void checkOwnIp(){
        InetAddress address1;
        try {
            address1 = InetAddress.getByName("monitoring.local");
            SERVER_NAME = address1.getHostAddress();
            monitoringDao.insertMgServers(address1.getHostAddress());
//            log.info("SERVER_NAME: [{}]", SERVER_NAME);
        } catch (UnknownHostException e) {
            log.info(e.getMessage());
        }
    }

    private void checkService(Service service, Boolean firstCheck) {
        try{
            String response = rest.getForObject(service.getServiceUrl(), String.class);
            String version;

            if(service.getServiceVersionPath() != null) {
                JsonNode responseJson = objectMapper.readTree(response);
                version = responseJson.at(service.getServiceVersionPath()).asText();
//                log.info("Version: [{}]", joins.at(service.getServiceVersionPath()).asText());
            }else {
                version = response;
//                log.info("Version: [{}]", result);
            }

            service.setServiceStatus( true );
            service.setServiceVersion( version );
        }catch (Exception e){
            log.info(e.getMessage());
            service.setServiceStatus( false );
            service.setServiceVersion( "0.0.0");
        }
        monitoringDao.updateMgServices(service);
    }

    /**
     * Добавление клиента в очередь на получение статусов сервисов
     * @return
     */
    public DeferredQuote addToQueue(){
        DeferredQuote dq = new DeferredQuote();
        responseBodyQueue.add(dq);
        return dq;
    }

}
