package water.ustc.interceptor;

import org.dom4j.*;
import org.dom4j.io.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogInterceptor {
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SAXReader sax = new SAXReader();
    private File logFile = new File(Thread.currentThread().getContextClassLoader().getResource("log.xml").getPath());
    private Document document;
    {
        try {
            document = sax.read(logFile);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
    private Element log = document.getRootElement();
    private Element newAction = log.addElement("action");
    private void saveDocument(Document document, File xmlFile) {
        try {
            Writer w = new OutputStreamWriter(new FileOutputStream(xmlFile));
            OutputFormat of = OutputFormat.createPrettyPrint();
            of.setEncoding("UTF-8");
            XMLWriter xw = new XMLWriter(w, of);
            xw.write(document);
            xw.flush();
            xw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void preAction(String actionName) {
        Element newActionName = newAction.addElement("name");
        newActionName.setText(actionName);
        Element startTime = newAction.addElement("s-time");
        Date st = new Date();
        startTime.setText(sdf.format(st));
        //this.saveDocument(document, logFile);
    }
    public void afterAction(String actionResult) {
        Element endTime = newAction.addElement("e-time");
        Date et = new Date();
        endTime.setText(sdf.format(et));
        Element newActionName = newAction.addElement("result");
        newActionName.setText(actionResult);
        this.saveDocument(document, logFile);
    }
}
