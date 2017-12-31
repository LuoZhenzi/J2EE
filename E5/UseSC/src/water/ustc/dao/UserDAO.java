package water.ustc.dao;

import sc.ustc.dao.BaseDAO;
import water.ustc.bean.UserBean;
import java.sql.*;

public class UserDAO extends BaseDAO {
    public UserDAO() {
        this.setDriver("com.mysql.jdbc.Driver");
        this.setUrl("jdbc:mysql://localhost:3306/user");
        this.setUserName("root");
        this.setUserPassword("080015");
    }
    @Override
    public Object query(String sql) {
        this.conn = this.openDBConnection();
        Statement stmt;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            UserBean result = new UserBean(rs.getString("userId"),
                    rs.getString("userName"), rs.getString("userPass"));
            return result;
        } catch (SQLException e) {
            return null;
        } finally {
            this.closeDBConnection();
        }
    }
    @Override
    public boolean update(String sql) {
        return false;
    }
    @Override
    public boolean insert(String sql) {
        return false;
    }
    @Override
    public boolean delete(String sql) {
        return false;
    }
}
