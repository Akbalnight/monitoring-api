package com.askute.services.monitoring.monitoring;

import com.askute.services.monitoring.monitoring.dao.MonitoringDao;
import com.askute.services.monitoring.monitoring.model.JsonService;
import com.askute.services.monitoring.monitoring.model.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class LoadServices {

    @Autowired
    MonitoringDao monitoringDao;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Value("${config}")
    private String CONFIG_PATH;

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
    public void init() {
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

                    jsonServices.add(js);
                    checkService(js, true);
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(fixedRate = 10000)
    public void checkServices(){
        for (int i = 0; i < jsonServices.size(); i++){
            checkService(jsonServices.get(i), false);
        }
        simpMessagingTemplate.convertAndSend("/topic/services", monitoringDao.selectMgServices());
    }




    public void checkService(JsonService jsonService, Boolean firstCheck) {
        HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);

        Service service = new Service();

        service.setId(jsonService.getId());
        service.setServiceName(jsonService.getName());

        try{
            ResponseEntity<String> responseEntity = rest.exchange(jsonService.getUrl(), HttpMethod.GET, requestEntity, String.class);
            service.setServiceStatus( true );
            service.setServiceVersion( responseEntity.getBody() );
        }catch (HttpClientErrorException e){
            service.setServiceStatus( false );
            service.setServiceVersion( "0.0.0");
        }

        if(firstCheck)
            monitoringDao.insertMgServices(service);
        else
            monitoringDao.updateMgServices(service);
    }
}
