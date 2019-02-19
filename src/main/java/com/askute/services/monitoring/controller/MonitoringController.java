package com.askute.services.monitoring.controller;

import com.askute.services.monitoring.audit.dao.AuditDao;
import com.askute.services.monitoring.audit.model.Audit;
import com.askute.services.monitoring.monitoring.LoadServices;
import com.askute.services.monitoring.monitoring.dao.MonitoringDao;
import com.askute.services.monitoring.monitoring.model.DeferredQuote;
import com.askute.services.monitoring.monitoring.model.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Base64;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * MonitoringController.java
 * Description: Контроллер поддержки проекта
 */

@RestController
class MonitoringController {

    private Logger log = LoggerFactory.getLogger(String.class);

    @Autowired
    private BuildProperties bp;

    @Autowired
    private AuditDao auditDao;

    @Autowired
    private MonitoringDao monitoringDao;

    @Autowired
    private LoadServices loadServices;

    @GetMapping("/version")
    public ResponseEntity<Object> save(){
        try {
            return ResponseEntity.ok().body(bp.getVersion());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при работе сервера");
        }
    }

    @PostMapping("/audit")
    public void addLog(@RequestBody Audit audit){
        audit.setUname(new String(Base64.getDecoder().decode(audit.getUname())));
        audit.setPwd(new String(Base64.getDecoder().decode(audit.getPwd())));
        auditDao.setSqlAddRowAuditToMgAudit(audit);
    }

    @GetMapping("/monitoring/list")
    public List<Service> getListServices(){
        return monitoringDao.selectMgServices();
    }

    @MessageMapping("/color")
    public void receiveColor(String message){
        log.info("message.getColorString() = " + message);
    }


    @GetMapping("/db")
    public String getDbUrl(){
        return monitoringDao.getDbUrl();
    }


    @GetMapping("/monitoring/poll/demo")
    public @ResponseBody DeferredQuote deferredResult() {
        return loadServices.addToQueue();
    }


}


