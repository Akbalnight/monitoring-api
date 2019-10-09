package com.askute.services.monitoring.monitoring;

import com.askute.services.monitoring.monitoring.dao.MonitoringDao;
import com.askute.services.monitoring.monitoring.model.DeferredQuote;
import com.askute.services.monitoring.monitoring.model.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
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

    @Value("${monitoring.server.name}")
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
//        try {
//            monitoringDao.deleteMgServices();

//            jsonServices = new ArrayList<>();

        this.rest = new RestTemplate();
        this.headers = new HttpHeaders();
        headers.set("accept", "text/plain");

        services = monitoringDao.selectByServerNameMgServices(SERVER_NAME);

//            File confJson = new File(CONFIG_PATH+"/services.json");
//            if(confJson.exists()) {
//                Path path = confJson.toPath();
//                byte[] data = Files.readAllBytes(path);
//                String strData = new String(data, "UTF-8");
//                JsonNode joins = objectMapper.readTree(strData);
//
//                for (int i = 0; i < joins.size(); i++) {
//                    JsonNode joinNode = joins.get(i);
//                    JsonService js = new JsonService();
//                    js.setId(joinNode.get("id").asInt());
//                    js.setName(joinNode.get("name").asText());
//                    js.setUrl(joinNode.get("url").asText());
//                    js.setKey(joinNode.get("key").asText());
//                    js.setServer(joinNode.get("server").asText());
//
//                    jsonServices.add(js);
//                    checkService(js, true);
//                }
//
//            }

//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Scheduled(fixedRate = 10000)
    private void checkServices(){
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

    private void checkService(Service service, Boolean firstCheck) {
        HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);

//        Service service = new Service();

//        service.setId(jsonService.getId());
//        service.setServiceName(jsonService.getName());
//        service.setServiceUrl(jsonService.getUrl());
//        service.setServiceKey(jsonService.getKey());
//        service.setServerId(jsonService.getServer());

        try{
            ResponseEntity<String> responseEntity = rest.exchange(service.getServiceUrl(), HttpMethod.GET, requestEntity, String.class);
            service.setServiceStatus( true );
            service.setServiceVersion( responseEntity.getBody() );
        }catch (Exception e){
            service.setServiceStatus( false );
            service.setServiceVersion( "0.0.0");
        }

//        if(firstCheck)
//            monitoringDao.insertMgServices(service);
//        else
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
