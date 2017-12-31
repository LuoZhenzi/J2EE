package sc.ustc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class BaseDAO {
    protected String driver;
    protected String url;
    protected String userName;
    protected String userPassword;
    protected Connection conn = null;
    public void setDriver(String driver) {
        this.driver = driver;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
    public Connection openDBConnection() {
        try {
            Class.forName(driver);
            this.conn = DriverManager.getConnection(url, userName, userPassword);
            return this.conn;
        } catch (ClassNotFoundException e) {
            return null;
        } catch (SQLException e) {
            return null;
        }
    }
    public boolean closeDBConnection() {
        try {
            this.conn.close();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    public abstract Object query(String sql);
    public abstract boolean insert(String sql);
    public abstract boolean update(String sql);
    public abstract boolean delete(String sql);
}
