package water.ustc.bean;

import org.dom4j.*;
import org.dom4j.io.SAXReader;
import java.beans.*;
import java.io.File;
import java.lang.reflect.*;
import java.util.List;

public class UserBean {
    private String userName;
    private String userPass;

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getUserPass() {
        return userPass;
    }
    public void setUserPass(String userPass) {
        this.userPass = userPass;
    }
    public boolean signIn() {
        Object result = null;
        SAXReader dSax = new SAXReader();
        File dXml = new File(Thread.currentThread().getContextClassLoader().getResource("di.xml").getPath());
        try {
            Document dDocument = dSax.read(dXml);
            Element dRoot = dDocument.getRootElement();
            List<Element> beanList = dRoot.elements("bean");
            Element bean;
            String beanId;
            for (int i = 0; i < beanList.size(); i++) {
                bean = beanList.get(i);
                beanId = bean.attributeValue("id");
                if (beanId.equals("userDAO")) {
                    Element field = bean.element("field");
                    String beanRef = field.attributeValue("bean-ref");
                    Element refBean;
                    String refBeanId;
                    for (int j = 0; j < beanList.size(); j++) {
                        refBean = beanList.get(j);
                        refBeanId = refBean.attributeValue("id");
                        if (refBeanId.equals(beanRef)) {
                            Class refBeanClass = Class.forName(refBean.attributeValue("class"));
                            Object refBeanObj = refBeanClass.newInstance();
                            Class beanClass = Class.forName(bean.attributeValue("class"));
                            Object beanObj = beanClass.newInstance();
                            PropertyDescriptor descriptor = new PropertyDescriptor(refBeanId, beanClass);
                            Method setMethod = descriptor.getWriteMethod();
                            setMethod.invoke(beanObj, refBeanObj);
                            Method beanMethod = beanClass.getMethod("query", String.class);
                            result = beanMethod.invoke(beanObj, userName);
                        }
                    }
                }
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
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
