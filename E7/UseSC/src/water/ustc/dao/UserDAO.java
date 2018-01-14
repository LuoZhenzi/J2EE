package water.ustc.dao;

import sc.ustc.dao.BaseDAO;
import sc.ustc.dao.Conversation;

public class UserDAO extends BaseDAO {
    private Conversation conv;

    public Conversation getConv() {
        return conv;
    }

    public void setConv(Conversation conv) {
        this.conv = conv;
    }
    @Override
    public Object query(String userName) {
        Object result = conv.getObject("water.ustc.bean.UserBean", userName);
        return result;
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
