package sc.ustc.controller;

import net.sf.cglib.proxy.*;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.List;

public class SimpleController extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter pw = response.getWriter();
        int i, j, k;
        String path1 = request.getServletPath();
        String path2 = path1.substring(path1.indexOf("/")+1, path1.lastIndexOf("."));
        SAXReader sax = new SAXReader();
        File cXml = new File(getServletContext().getRealPath("/WEB-INF/classes/controller.xml"));
        try {
            Document document = sax.read(cXml);
            Element root = document.getRootElement();
            List<Element> actionList = root.element("controller").elements("action");
            Element action;
            String actionName;
            for (i = 0; i < actionList.size(); i++) {
                action = actionList.get(i);
                actionName = action.attributeValue("name");
                if (actionName.equals(path2)) {
                    Element interceptorRef = action.element("interceptor-ref");
                    Element interceptor;
                    Class interceptorClass;
                    Object interceptorObject = null;
                    Method interceptorPreDo = null;
                    Method interceptorAfterDo = null;
                    if (interceptorRef != null) {
                        List<Element> interceptorList = root.elements("interceptor");
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
                    Class actionClass = Class.forName(action.attributeValue("class"));
                    Object actionObject = actionClass.newInstance();
                    Method actionMethod = actionClass.getMethod(action.attributeValue("method"));
                    Method finalInterceptorPreDo = interceptorPreDo;
                    Object finalInterceptorObject = interceptorObject;
                    String finalActionName = actionName;
                    Method finalInterceptorAfterDo = interceptorAfterDo;
                    class TargetInterceptor implements MethodInterceptor {
                        public String intercept(Object object, Method method, Object[] params, MethodProxy proxy) throws Throwable {
                            if (finalInterceptorPreDo != null) {
                                finalInterceptorPreDo.invoke(finalInterceptorObject, finalActionName);
                            }
                            String actionResult = (String) proxy.invokeSuper(object, params);
                            if (finalInterceptorAfterDo != null) {
                                finalInterceptorAfterDo.invoke(finalInterceptorObject, actionResult);
                            }
                            return actionResult;
                        }
                    }
                    Enhancer enhancer =new Enhancer();
                    enhancer.setSuperclass(actionObject.getClass());
                    enhancer.setCallback(new TargetInterceptor());
                    Object actionProxy = enhancer.create();
                    String actionResult = (String) actionMethod.invoke(actionProxy, new Object[]{});
                    //String actionResult = (String) actionMethod.invoke(actionObject, new Object[]{});
                    List<Element> resultList = action.elements("result");
                    for (j = 0; j < resultList.size(); j++) {
                        Element result = resultList.get(j);
                        if (result.attributeValue("name").equals(actionResult)) {
                            if (result.attributeValue("type").equals("forward")) {
                                request.getRequestDispatcher(result.attributeValue("value")).forward(request, response);
                            }
                            if (result.attributeValue("type").equals("redirect")) {
                                response.sendRedirect(result.attributeValue("value"));
                            }
                            break;
                        }
                    }
                    if (j >= resultList.size()) {
                        pw.print("<html><head><title></title><head><body>没有请求的资源。</body></html>");
                    }
                    break;
                }
            }
            if (i >= actionList.size()) {
                pw.print("<html><head><title></title><head><body>不可识别的 action 请求。</body></html>");
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
