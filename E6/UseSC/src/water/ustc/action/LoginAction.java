package water.ustc.action;

import water.ustc.bean.UserBean;

public class LoginAction {
    public String handleLogin(String userName, String password) {
        UserBean userBean = new UserBean(userName, password);
        if (userBean.signIn()) {
            return "success";
        } else {
            return "failure";
        }
    }
}
