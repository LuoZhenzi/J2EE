package water.ustc.bean;

import water.ustc.dao.UserDAO;

public class UserBean {
    //private String userId;
    private String userName;
    private String userPass;

    public UserBean() {
    }
    public UserBean(String userName, String userPass) {
        //this.userId = userId;
        this.userName = userName;
        this.userPass = userPass;
    }
    public boolean signIn() {
        UserDAO userDAO = new UserDAO();
        Object result = userDAO.query(userName);
        UserBean resultUserBean = (UserBean) result;
        if (resultUserBean == null) {
            return false;
        } else if (resultUserBean.userPass.equals(userPass)) {
            return true;
        } else {
            return false;
        }
    }
}
