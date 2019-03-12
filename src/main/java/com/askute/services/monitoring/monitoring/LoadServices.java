package com.askute.services.monitoring.monitoring;

import com.askute.services.monitoring.monitoring.dao.MonitoringDao;
import com.askute.services.monitoring.monitoring.model.DeferredQuote;
import com.askute.services.monitoring.monitoring.model.JsonService;
import com.askute.services.monitoring.monitoring.model.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class LoadServices {

    @Autowired
    MonitoringDao monitoringDao;

    @Value("${config}")
    private String CONFIG_PATH;

    /**
     * Очередь клиентов на получение статусов сервисов
     */
    private final Queue<DeferredQuote> responseBodyQueue = new ConcurrentLinkedQueue<>();

    private final ObjectMapper objectMapper;

    private List<JsonService> jsonServices;
    private RestTemplate rest;
    private HttpHeaders headers;
    private HttpStatus status;


    @Autowired
    public LoadServices(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @PostConstruct
    private void init() {
        try {
            monitoringDao.deleteMgServices();

            jsonServices = new ArrayList<>();
            this.rest = new RestTemplate();
            this.headers = new HttpHeaders();

            Path path;

            File confJson = new File(CONFIG_PATH+"/services.json");
            if(confJson.exists()) {
                path = confJson.toPath();
                byte[] data = Files.readAllBytes(path);
                String strData = new String(data, "UTF-8");
                JsonNode joins = objectMapper.readTree(strData);

                for (int i = 0; i < joins.size(); i++) {
                    JsonNode joinNode = joins.get(i);
                    JsonService js = new JsonService();
                    js.setId(joinNode.get("id").asInt());
                    js.setName(joinNode.get("name").asText());
                    js.setUrl(joinNode.get("url").asText());
                    js.setKey(joinNode.get("key").asText());

                    jsonServices.add(js);
                    checkService(js, true);
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(fixedRate = 10000)
    private void checkServices(){
        for (int i = 0; i < jsonServices.size(); i++){
            checkService(jsonServices.get(i), false);
        }
        List<Service> Ls = monitoringDao.selectMgServices();

        for (DeferredQuote result : responseBodyQueue) {
            result.setResult(Ls);
            responseBodyQueue.remove(result);
        }

    }

    private void checkService(JsonService jsonService, Boolean firstCheck) {
        HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);

        Service service = new Service();

        service.setId(jsonService.getId());
        service.setServiceName(jsonService.getName());
        service.setServiceUrl(jsonService.getUrl());
        service.setServiceKey(jsonService.getKey());

        try{
            ResponseEntity<String> responseEntity = rest.exchange(jsonService.getUrl(), HttpMethod.GET, requestEntity, String.class);
            service.setServiceStatus( true );
            service.setServiceVersion( responseEntity.getBody() );
        }catch (Exception e){
            service.setServiceStatus( false );
            service.setServiceVersion( "0.0.0");
        }

        if(firstCheck)
            monitoringDao.insertMgServices(service);
        else
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
