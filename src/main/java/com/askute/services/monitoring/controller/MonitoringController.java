package com.askute.services.monitoring.controller;

import com.askute.services.monitoring.audit.dao.AuditDao;
import com.askute.services.monitoring.audit.model.Audit;
import com.askute.services.monitoring.monitoring.LoadServices;
import com.askute.services.monitoring.monitoring.dao.MonitoringDao;
import com.askute.services.monitoring.monitoring.model.DeferredQuote;
import com.askute.services.monitoring.monitoring.model.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;

/**
 * MonitoringController.java
 * Description: Контроллер поддержки проекта
 */

@RestController
class MonitoringController {

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
        return monitoringDao.selectAllMgServices();
    }

    @GetMapping("/monitoring/list/poll")
    public @ResponseBody DeferredQuote deferredResult() {
        return loadServices.addToQueue();
    }

    @GetMapping("/db")
    public String getDbUrl(){
        return monitoringDao.getDbUrl();
    }

}


