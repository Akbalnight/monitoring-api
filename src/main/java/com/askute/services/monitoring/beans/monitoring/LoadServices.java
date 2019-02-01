package com.askute.services.monitoring.beans.monitoring;

import com.askute.services.monitoring.beans.monitoring.dao.MonitoringDao;
import com.askute.services.monitoring.beans.monitoring.model.JsonService;
import com.askute.services.monitoring.beans.monitoring.model.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class LoadServices {

    @Autowired
    MonitoringDao monitoringDao;

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
            jsonServices = new ArrayList<>();
            this.rest = new RestTemplate();
            this.headers = new HttpHeaders();

            Path path = new File(getClass().getResource("/services.json").toURI()).toPath();
            byte[] data = Files.readAllBytes(path);
            String strData = new String(data, "UTF-8");
            JsonNode joins = objectMapper.readTree(strData);
            for (int i = 0; i < joins.size(); i++) {
                List<String> tables = new ArrayList<>();
                JsonNode joinNode = joins.get(i);
                System.out.print(joinNode.get("id").asInt() + "\t");
                System.out.print(joinNode.get("name").asText() + "\t\t\t");
                System.out.println(joinNode.get("url").asText());

                JsonService js = new JsonService();
                js.setId(joinNode.get("id").asInt());
                js.setName(joinNode.get("name").asText());
                js.setUrl(joinNode.get("url").asText());

                jsonServices.add(js);
            }

            for (int i = 0; i < jsonServices.size(); i++){
                checkService(jsonServices.get(i), true);
            }

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(fixedRate = 10000)
    public void checkServices(){
        for (int i = 0; i < jsonServices.size(); i++){
            checkService(jsonServices.get(i), false);
        }
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
