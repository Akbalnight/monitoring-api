package com.askute.services.monitoring.beans.audit.dao;

import com.askute.services.monitoring.beans.audit.model.Audit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Repository
public class AuditDao {

    @Autowired
    private DataSource dataSource;

    private NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * SQL запрос на добавление в лог
     */
    private static final String SQL_ADD_ROW_AUDIT_TO_MG_AUDIT = "INSERT " +
            " INTO mg_audit(uname, pwd, write_date) " +
            " VALUES (:uname, :pwd, NOW());";

    @PostConstruct
    public void init() {

        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }


    public void setSqlAddRowAuditToMgAudit(Audit audit)
    {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("uname", audit.getUname());
            params.addValue("pwd", audit.getPwd());
            jdbcTemplate.update(SQL_ADD_ROW_AUDIT_TO_MG_AUDIT, params);
        }
        catch (Exception e){
            System.out.println("Ошибка записи в лог");
            System.out.println(e);
        }
    }

}
