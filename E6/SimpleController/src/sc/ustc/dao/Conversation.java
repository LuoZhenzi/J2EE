package sc.ustc.dao;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.HashMap;

public class Conversation {
    private Configuration config = new Configuration();
    private Connection getConnection(String sqlName) {
        Connection conn = null;
        HashMap<String, String> jdbcMap = config.getJdbc(sqlName);
        String driverClass = jdbcMap.get("driver_class");
        String urlPath = jdbcMap.get("url_path");
        String dbUserName = jdbcMap.get("db_username");
        String dbUserPass = jdbcMap.get("db_userpassword");
        try {
            Class.forName(driverClass);
            conn = DriverManager.getConnection(urlPath, dbUserName, dbUserPass);
            //conn = DriverManager.getConnection(urlPath);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }
    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public Object getObject(String className, String userName) {
        Connection conn = getConnection("mysql");
        HashMap<String, String> tableMap = config.getTable(className);
        String querySql = "SELECT * FROM " + tableMap.get("tableName")
                + " WHERE " + tableMap.get("userName") + "='" + userName + "'";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(querySql);
            rs.next();
            Class resultClass = Class.forName(className);
            Object resultObj = resultClass.newInstance();
            Field[] fields = resultClass.getDeclaredFields();
            for(Field field:fields){
                field.setAccessible(true);
                field.set(resultObj, rs.getString(tableMap.get(field.getName())));
            }
            return resultObj;
        } catch (SQLException e) {
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        } catch (InstantiationException e) {
            return null;
        } finally {
            closeConnection(conn);
        }
    }
}
