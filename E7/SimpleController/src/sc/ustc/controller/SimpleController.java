package sc.ustc.controller;

import org.dom4j.*;
import org.dom4j.io.SAXReader;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import java.beans.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.List;

public class SimpleController extends HttpServlet {
    private static void transformXml(String xmlFileName, String xslFileName, String htmlFileName) {
        TransformerFactory tFac = TransformerFactory.newInstance();
        Source xslSource = new StreamSource(xslFileName);
        try {
            Transformer tf = tFac.newTransformer(xslSource);
            File xmlFile = new File(xmlFileName);
            File htmlFile = new File(htmlFileName);
            Source tSource = new StreamSource(xmlFile);
            Result tResult = new StreamResult(htmlFile);
            tf.transform(tSource, tResult);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }
    private static void setProperty(Class beanClass, Object beanObject, String propertyName, Object propertyObj) {
        try {
            PropertyDescriptor descriptor = new PropertyDescriptor(propertyName, beanClass);
            Method setMethod = descriptor.getWriteMethod();
            setMethod.invoke(beanObject, propertyObj);
        } catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter pw = response.getWriter();
        int i, j, k, l;
        String userName = request.getParameter("userName");
        String password = request.getParameter("password");
        String requestPath = request.getServletPath();
        String requestName = requestPath.substring(requestPath.indexOf("/")+1, requestPath.lastIndexOf("."));
        String actionResult = null;
        SAXReader cSax = new SAXReader();
        File cXml = new File(getServletContext().getRealPath("/WEB-INF/classes/controller.xml"));
        try {
            Document cDocument = cSax.read(cXml);
            Element cRoot = cDocument.getRootElement();
            List<Element> actionList = cRoot.element("controller").elements("action");
            Element action;
            String actionName;
            for (i = 0; i < actionList.size(); i++) {
                action = actionList.get(i);
                actionName = action.attributeValue("name");
                if (actionName.equals(requestName)) {
                    Element interceptorRef = action.element("interceptor-ref");
                    Element interceptor;
                    Class interceptorClass;
                    Object interceptorObject = null;
                    Method interceptorPreDo = null;
                    Method interceptorAfterDo = null;
                    if (interceptorRef != null) {
                        List<Element> interceptorList = cRoot.elements("interceptor");
                        for (k = 0; k < interceptorList.size(); k++) {
                            interceptor = interceptorList.get(k);
                            if (interceptor.attributeValue("name").equals(interceptorRef.attributeValue("name"))) {
                                interceptorClass = Class.forName(interceptor.attributeValue("class"));
                                interceptorObject = interceptorClass.newInstance();
                                interceptorPreDo = interceptorClass.getMethod(interceptor.attributeValue("predo"), String.class);
                                interceptorAfterDo = interceptorClass.getMethod(interceptor.attributeValue("afterdo"), String.class);
                                break;
                            }
                        }
                    }
                    SAXReader dSax = new SAXReader();
                    File dXml = new File(getServletContext().getRealPath("/WEB-INF/classes/di.xml"));
                    Document dDocument = dSax.read(dXml);
                    Element dRoot = dDocument.getRootElement();
                    List<Element> beanList = dRoot.elements("bean");
                    Element bean;
                    String beanId;
                    for (j = 0; j < beanList.size(); j++) {
                        bean = beanList.get(j);
                        beanId = bean.attributeValue("id");
                        if (beanId.equals(actionName)) {
                            Element field = bean.element("field");
                            String beanRef = field.attributeValue("bean-ref");
                            Element refBean;
                            String refBeanId;
                            for (k = 0; k < beanList.size(); k++) {
                                refBean = beanList.get(k);
                                refBeanId = refBean.attributeValue("id");
                                if (refBeanId.equals(beanRef)) {
                                    Class refBeanClass = Class.forName(refBean.attributeValue("class"));
                                    Object refBeanObj = refBeanClass.newInstance();
                                    setProperty(refBeanClass, refBeanObj, "userName", userName);
                                    setProperty(refBeanClass, refBeanObj, "userPass", password);
                                    Class beanClass = Class.forName(bean.attributeValue("class"));
                                    Object beanObj = beanClass.newInstance();
                                    setProperty(beanClass, beanObj, "userBean", refBeanObj);
                                    Method beanMethod = beanClass.getMethod(action.attributeValue("method"),
                                            new Class[0]);
                                    if (interceptorPreDo != null) {
                                        interceptorPreDo.invoke(interceptorObject, actionName);
                                    }
                                    actionResult = (String) beanMethod.invoke(beanObj, new Object[]{});
                                    if (interceptorAfterDo != null) {
                                        interceptorAfterDo.invoke(interceptorObject, actionResult);
                                    }
                                    break;
                                }
                            }
                            if (k >= beanList.size()) {
                                Class beanClass = Class.forName(bean.attributeValue("class"));
                                Object beanObj = beanClass.newInstance();
                                Method beanMethod = beanClass.getMethod(action.attributeValue("method"), new Class[0]);
                                if (interceptorPreDo != null) {
                                    interceptorPreDo.invoke(interceptorObject, actionName);
                                }
                                actionResult = (String) beanMethod.invoke(beanObj, new Object[]{});
                                if (interceptorAfterDo != null) {
                                    interceptorAfterDo.invoke(interceptorObject, actionResult);
                                }
                            }
                            break;
                        }
                    }
                    if (j >= beanList.size()) {
                        Class actionClass = Class.forName(action.attributeValue("class"));
                        Object actionObject = actionClass.newInstance();
                        Method actionMethod = actionClass.getMethod(action.attributeValue("method"), new Class[0]);
                        if (interceptorPreDo != null) {
                            interceptorPreDo.invoke(interceptorObject, actionName);
                        }
                        actionResult = (String) actionMethod.invoke(actionObject, new Object[]{});
                        if (interceptorAfterDo != null) {
                            interceptorAfterDo.invoke(interceptorObject, actionResult);
                        }
                    }
                    List<Element> resultList = action.elements("result");
                    for (l = 0; l < resultList.size(); l++) {
                        Element result = resultList.get(l);
                        if (result.attributeValue("name").equals(actionResult)) {
                            String resultValue = result.attributeValue("value");
                            if (resultValue.endsWith("_view.xml")) {
                                String xmlFileName = getServletContext().getRealPath(resultValue);
                                String xslFileName = getServletContext()
                                        .getRealPath("xmltohtml.xsl");
                                String newResultValue = resultValue.substring(0,
                                        resultValue.lastIndexOf("_")).concat(".html");
                                String htmlFileName = getServletContext().getRealPath(newResultValue);
                                this.transformXml(xmlFileName, xslFileName, htmlFileName);
                                if (result.attributeValue("type").equals("forward")) {
                                    request.getRequestDispatcher(newResultValue).forward(request,
                                            response);
                                }
                                if (result.attributeValue("type").equals("redirect")) {
                                    response.sendRedirect(newResultValue);
                                }
                            }
                            else {
                                if (result.attributeValue("type").equals("forward")) {
                                    request.getRequestDispatcher(result.
                                            attributeValue("value")).forward(request, response);
                                }
                                if (result.attributeValue("type").equals("redirect")) {
                                    response.sendRedirect(result.attributeValue("value"));
                                }
                            }
                            break;
                        }
                    }
                    if (l >= resultList.size()) {
                        pw.print("<html><head><title></title><head><body>没有请求的资源。</body></html>");
                    }
                    break;
                }
            }
            if (i >= actionList.size()) {
                pw.print("<html><head><title></title><head><body>无法识别该请求。</body></html>");
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
