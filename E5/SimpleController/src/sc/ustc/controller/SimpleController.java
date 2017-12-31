package sc.ustc.controller;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.*;
import java.lang.reflect.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;

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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter pw = response.getWriter();
        int i, j, k;
        String userName = request.getParameter("userName");
        String password = request.getParameter("password");
        String path1 = request.getServletPath();
        String path2 = path1.substring(path1.indexOf("/")+1, path1.lastIndexOf("."));
        DocumentBuilderFactory a = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder b = a.newDocumentBuilder();
            Document document = b.parse(getServletContext().getRealPath("/WEB-INF/classes/controller.xml"));
            NodeList actionList = document.getElementsByTagName("action");
            Element action;
            String actionName;
            for (i = 0; i < actionList.getLength(); i++) {
                action = (Element) actionList.item(i);
                actionName = action.getAttribute("name");
                if (actionName.equals(path2)) {
                    Element interceptorRef = (Element) action.getElementsByTagName("interceptor-ref").item(0);
                    Element interceptor;
                    Class interceptorClass;
                    Object interceptorObject = null;
                    Method interceptorPreDo = null;
                    Method interceptorAfterDo = null;
                    if (interceptorRef != null) {
                        NodeList interceptorList = document.getElementsByTagName("interceptor");
                        for (k = 0; k < interceptorList.getLength(); k++) {
                            interceptor = (Element) interceptorList.item(k);
                            if (interceptor.getAttribute("name").equals(interceptorRef.getAttribute("name"))) {
                                interceptorClass = Class.forName(interceptor.getAttribute("class"));
                                interceptorObject = interceptorClass.newInstance();
                                interceptorPreDo = interceptorClass.getMethod(interceptor.getAttribute("predo"), String.class);
                                interceptorAfterDo = interceptorClass.getMethod(interceptor.getAttribute("afterdo"), String.class);
                                break;
                            }
                        }
                    }
                    if (interceptorPreDo != null) {
                        interceptorPreDo.invoke(interceptorObject, actionName);
                    }
                    Class actionClass = Class.forName(action.getAttribute("class"));
                    Object actionObject = actionClass.newInstance();
                    Method actionMethod = actionClass.getMethod(action.getAttribute("method"), String.class, String.class);
                    String actionResult = (String) actionMethod.invoke(actionObject, userName, password);
                    NodeList resultList = action.getElementsByTagName("result");
                    for (j = 0; j < resultList.getLength(); j++) {
                        Element result = (Element) resultList.item(j);
                        if (result.getAttribute("name").equals(actionResult)) {
                            String resultValue = result.getAttribute("value");
                            if (resultValue.endsWith("_view.xml")) {
                                String xmlFileName = getServletContext().getRealPath(resultValue);
                                String xslFileName = getServletContext().getRealPath("xmltohtml.xsl");
                                String newResultValue = resultValue.substring(0, resultValue.lastIndexOf("_")).concat(".html");
                                String htmlFileName = getServletContext().getRealPath(newResultValue);
                                this.transformXml(xmlFileName, xslFileName, htmlFileName);
                                if (result.getAttribute("type").equals("forward")) {
                                    request.getRequestDispatcher(newResultValue).forward(request, response);
                                }
                                if (result.getAttribute("type").equals("redirect")) {
                                    response.sendRedirect(newResultValue);
                                }
                            }
                            else {
                                if (result.getAttribute("type").equals("forward")) {
                                    request.getRequestDispatcher(result.getAttribute("value")).forward(request, response);
                                }
                                if (result.getAttribute("type").equals("redirect")) {
                                    response.sendRedirect(result.getAttribute("value"));
                                }
                            }
                            break;
                        }
                    }
                    if (j >= resultList.getLength()) {
                        pw.print("<html><head><title></title><head><body>没有请求的资源。</body></html>");
                    }
                    if (interceptorAfterDo != null) {
                        interceptorAfterDo.invoke(interceptorObject, actionResult);
                    }
                    break;
                }
            }
            if (i >= actionList.getLength()) {
                pw.print("<html><head><title></title><head><body>不可识别的 action 请求。</body></html>");
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
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