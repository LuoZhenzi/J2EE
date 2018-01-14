package water.ustc.action;

import water.ustc.bean.UserBean;

public class LoginAction {
    private UserBean userBean;
    public UserBean getUserBean() {
        return userBean;
    }
    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }
    public String handleLogin() {
        if (userBean.signIn()) {
            return "success";
        } else {
            return "failure";
        }
    }
}
