package com.askute.services.monitoring.monitoring.dao;

import com.askute.services.monitoring.monitoring.model.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.RowCountCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MonitoringDao {

    @Autowired
    private DataSource dataSource;

    private NamedParameterJdbcTemplate jdbcTemplate;

    private static final String SQL_INSERT_MG_SERVICES = "INSERT " +
            "INTO public.mg_services( id, service_name, service_url, service_key, service_version, service_status, update_time) " +
            "VALUES (:id, :service_name, :service_url, :service_key, :service_version, :service_status, NOW())";

    private static final String SQL_UPDATE_MG_SERVICES = "UPDATE public.mg_services " +
            "SET service_name=:service_name, service_version=:service_version, service_status=:service_status, update_time=NOW() " +
            "WHERE id=:id";

    private static final String SQL_SELECT_MG_SERVICES = "SELECT * FROM public.mg_services order by id asc";

    private static final String SQL_DELETE_MG_SERVICES = "DELETE FROM public.mg_services";

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
//            System.out.println("Ошибка записи в mg_services");
            System.out.println(e);
        }
    }

    public void updateMgServices (Service service){
        try {
            jdbcTemplate.update(SQL_UPDATE_MG_SERVICES, getServiceParams(service));
        }
        catch (Exception e){
//            System.out.println("Ошибка обновления mg_services");
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
            // Если таблица пустая заполним ее
            RowCountCallbackHandler countCallback = new RowCountCallbackHandler();
            jdbcTemplate.query(SQL_DELETE_MG_SERVICES, countCallback);
        }
        catch (Exception e){
//            System.out.println("Ошибка очистки mg_services");
            System.out.println(e);
        }
    }

    public String getDbUrl(){
        try {
            return dataSource.getConnection().getMetaData().getURL();
        }catch (SQLException e){
            System.out.println(e);
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
