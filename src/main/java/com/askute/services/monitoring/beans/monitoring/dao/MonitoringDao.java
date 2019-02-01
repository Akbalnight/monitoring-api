package com.askute.services.monitoring.beans.monitoring.dao;

import com.askute.services.monitoring.beans.audit.model.Audit;
import com.askute.services.monitoring.beans.monitoring.model.JsonService;
import com.askute.services.monitoring.beans.monitoring.model.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class MonitoringDao {

    @Autowired
    private DataSource dataSource;

    private NamedParameterJdbcTemplate jdbcTemplate;

    private static final String SQL_INSERT_MG_SERVICES = "INSERT " +
            "INTO public.mg_services( id, service_name, service_version, service_status, update_time) " +
            "VALUES (:id, :service_name, :service_version, :service_status, NOW())";

    private static final String SQL_UPDATE_MG_SERVICES = "UPDATE public.mg_services " +
            "SET service_name=:service_name, service_version=:service_version, service_status=:service_status, update_time=NOW() " +
            "WHERE id=:id";

    private static final String SQL_SELECT_MG_SERVICES = "SELECT * FROM public.mg_services order by id asc";

    @PostConstruct
    public void init() {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public void insertMgServices (Service service){
        try {
            jdbcTemplate.update(SQL_INSERT_MG_SERVICES, getServiceParams(service));
        }
        catch (DuplicateKeyException e){}
        catch (Exception e){
            System.out.println("Ошибка записи в mg_services");
            System.out.println(e);
        }
    }

    public void updateMgServices (Service service){
        try {
            jdbcTemplate.update(SQL_UPDATE_MG_SERVICES, getServiceParams(service));
        }
        catch (Exception e){
            System.out.println("Ошибка обновления mg_services");
            System.out.println(e);
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
                result.add( new Service(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getBoolean(4)));
            }
            while (rs.next());
        });

        return result;
    }

    private MapSqlParameterSource getServiceParams(Service service){
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", service.getId());
        params.addValue("service_name", service.getServiceName());
        params.addValue("service_version", service.getServiceVersion());
        params.addValue("service_status", service.getServiceStatus());
        return params;
    }
    }
