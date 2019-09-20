package com.askute.services.monitoring.monitoring.dao;

import com.askute.services.monitoring.monitoring.model.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Repository
public class MonitoringDao {

    @Value("${monitoring.dbtable}")
    private String databaseTable;

    @Autowired
    private DataSource dataSource;

    private Logger log = LoggerFactory.getLogger(MonitoringDao.class);

    private NamedParameterJdbcTemplate jdbcTemplate;

    private final String SQL_CHECK_MG_SERVICES = "select count(*) from " + databaseTable;

    private final String SQL_INSERT_MG_SERVICES = "INSERT " +
            "INTO " + databaseTable + " (id, service_name, service_url, service_key, service_version, service_status, update_time) " +
            "VALUES (:id, :service_name, :service_url, :service_key, :service_version, :service_status, NOW())";

    private final String SQL_UPDATE_MG_SERVICES = "UPDATE " + databaseTable +
            " SET service_name=:service_name, service_version=:service_version, service_status=:service_status, update_time=NOW() " +
            "WHERE id=:id";

    private final String SQL_SELECT_MG_SERVICES = "SELECT * FROM " + databaseTable + " order by id asc";

    private final String SQL_DELETE_MG_SERVICES = "DELETE FROM " + databaseTable;

    @PostConstruct
    public void init() {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        jdbcTemplate.queryForObject(SQL_CHECK_MG_SERVICES, (HashMap) null, Long.class);
    }

    public void insertMgServices (Service service){
        try {
            jdbcTemplate.update(SQL_INSERT_MG_SERVICES, getServiceParams(service));
        }
        catch (DuplicateKeyException e){}
        catch (Exception e){
            log.error(e.toString());
        }
    }

    public void updateMgServices (Service service){
        try {
            jdbcTemplate.update(SQL_UPDATE_MG_SERVICES, getServiceParams(service));
        }
        catch (Exception e){
            log.error(e.toString());
        }
    }

    public List<Service> selectMgServices (){
        List<Service> result = new ArrayList<>();
        jdbcTemplate.query(SQL_SELECT_MG_SERVICES, (ResultSet rs) ->
        {
            if (rs.isBeforeFirst())
            {
                return;
            }
            do
            {
                result.add( new Service(
                        rs.getInt("id"),
                        rs.getString("service_name"),
                        rs.getString("service_url"),
                        rs.getString("service_key"),
                        rs.getString("service_version"),
                        rs.getBoolean("service_status")));
            }
            while (rs.next());
        });

        return result;
    }

    public void deleteMgServices (){
        try {
            jdbcTemplate.queryForObject(SQL_DELETE_MG_SERVICES, (HashMap) null, Long.class);
        }
        catch (Exception e){
            log.error(e.toString());
        }
    }

    public String getDbUrl(){
        try {
            return dataSource.getConnection().getMetaData().getURL();
        }catch (SQLException e){
            log.error(e.toString());
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
        return params;
    }
    }
