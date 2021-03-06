package com.askute.services.monitoring.monitoring.dao;

import com.askute.services.monitoring.monitoring.model.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Repository
public class MonitoringDao {

    @Value("${monitoring.data.table}")
    private String tableName;

    @Value("${monitoring.servers.table}")
    private String tableServerName;

    @Autowired
    private DataSource dataSource;

    private Logger log = LoggerFactory.getLogger(MonitoringDao.class);
    private final RowMapper<Service> rowMapper = BeanPropertyRowMapper.newInstance(Service.class);

    private NamedParameterJdbcTemplate jdbcTemplate;

    private String SQL_CHECK_MG_SERVICES;
    private String SQL_INSERT_MG_SERVICES;
    private String SQL_UPDATE_MG_SERVICES;
    private String SQL_SELECT_MG_SERVICES;
    private String SQL_DELETE_MG_SERVICES;
    private String SQL_INSERT_MG_SERVERS;

    @PostConstruct
    public void init() {
        SQL_CHECK_MG_SERVICES = "SELECT COUNT(*) FROM " + tableName;

        SQL_INSERT_MG_SERVICES = "INSERT " +
                "INTO " + tableName + " (id, service_name, service_url, service_key, service_version, service_status, update_time, server_id) " +
                "VALUES (:id, :service_name, :service_url, :service_key, :service_version, :service_status, NOW(), :server_id)";

        SQL_UPDATE_MG_SERVICES = "UPDATE " + tableName +
                " SET service_version=:service_version, service_status=:service_status, update_time=NOW() " +
                "WHERE id=:id";

        SQL_SELECT_MG_SERVICES = "SELECT * FROM " + tableName + " %s ORDER BY code ASC";

        SQL_DELETE_MG_SERVICES = "DELETE FROM " + tableName;

        SQL_INSERT_MG_SERVERS = String.format("" +
                "INSERT INTO %s (server_ip, update_time) " +
                    "VALUES (:serverIp, current_timestamp) " +
                    "on conflict  (server_ip) do " +
                    "update set update_time = current_timestamp;", tableServerName);

        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        jdbcTemplate.queryForObject(SQL_CHECK_MG_SERVICES, Collections.emptyMap(), Long.class);
    }

    public void insertMgServices (Service service){
        try {
            jdbcTemplate.update(SQL_INSERT_MG_SERVICES, getServiceParams(service));
        }
        catch (DuplicateKeyException e){}
        catch (Exception e){
            log.error(e.getMessage());
        }
    }

    public void updateMgServices (Service service){
        try {
            jdbcTemplate.update(SQL_UPDATE_MG_SERVICES, getServiceParams(service));
        }
        catch (Exception e){
            log.error(e.getMessage());
        }
    }

    public List<Service> selectByServerNameMgServices (String serverName){
        return jdbcTemplate.query(String.format(SQL_SELECT_MG_SERVICES, "where server_name = :server_name"), new MapSqlParameterSource("server_name", serverName), rowMapper);
    }

    public List<Service> selectAllMgServices (){
        return jdbcTemplate.query(String.format(SQL_SELECT_MG_SERVICES, ""), rowMapper);
    }

    public void deleteMgServices (){
        try {
            jdbcTemplate.queryForObject(SQL_DELETE_MG_SERVICES, Collections.emptyMap(), Long.class);
        }
        catch (Exception e){
            log.error(e.getMessage());
        }
    }

    public void insertMgServers(String serverIp){
        try {
            jdbcTemplate.update(SQL_INSERT_MG_SERVERS, new MapSqlParameterSource("serverIp", serverIp));
        }
        catch (DuplicateKeyException e){}
        catch (Exception e){
            log.error(e.getMessage());
        }
    }

    public String getDbUrl(){
        try {
            return dataSource.getConnection().getMetaData().getURL();
        }catch (SQLException e){
            log.error(e.getMessage());
            return "";
        }
    }

    private MapSqlParameterSource getServiceParams(Service service){
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", service.getId());
        params.addValue("service_name", service.getServiceName());
        params.addValue("service_url", service.getServiceUrl());
        params.addValue("service_key", service.getServiceKey());
        params.addValue("service_version", service.getServiceVersion());
        params.addValue("service_status", service.getServiceStatus());
        params.addValue("server_name", service.getServerName());
        return params;
    }
}
