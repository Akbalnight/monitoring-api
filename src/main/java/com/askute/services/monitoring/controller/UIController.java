package com.askute.services.monitoring.controller;


import com.askute.services.monitoring.monitoring.dao.MonitoringDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UIController {

    @Autowired
    private MonitoringDao monitoringDao;

    @GetMapping("/")
    public String main(Model model) {
        model.addAttribute("services", monitoringDao.selectMgServices());

        return "index"; //view
    }
}
