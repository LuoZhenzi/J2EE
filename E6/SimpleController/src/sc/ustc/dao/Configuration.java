package sc.ustc.dao;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import java.io.File;
import java.util.HashMap;
import java.util.List;

public class Configuration {
    private SAXReader saxReader = new SAXReader();
    private File orXml = new File(Thread.currentThread().getContextClassLoader()
            .getResource("or_mapping.xml").getPath());
    private Document document;
    {
        try {
            document = saxReader.read(orXml);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
    private Element root = document.getRootElement();
    public HashMap<String, String> getJdbc(String sqlName) {
        HashMap<String, String> jdbcMap = new HashMap<String, String>();
        List<Element> jdbcInfoList = root.elements("jdbc");
        Element jdbcInfo = null;
        for (int i = 0; i < jdbcInfoList.size(); i++) {
            jdbcInfo = jdbcInfoList.get(i);
            if (jdbcInfo.element("property").elementText("value").contains(sqlName))
                break;
        }
        List<Element> jdbcProList = jdbcInfo.elements("property");
        for (int i = 0; i < jdbcProList.size(); i++) {
            Element jdbcPro = jdbcProList.get(i);
            jdbcMap.put(jdbcPro.elementText("name"), jdbcPro.elementText("value"));
        }
        return jdbcMap;
    }
    public HashMap<String, String> getTable(String className) {
        HashMap<String, String> tableMap = new HashMap<String, String>();
        List<Element> classInfoList = root.elements("class");
        Element classInfo = null;
        for (int i = 0; i < classInfoList.size(); i++) {
            classInfo = classInfoList.get(i);
            if (classInfo.elementText("name").equals(className)) {
                tableMap.put("tableName", classInfo.elementText("table"));
                break;
            }
        }
        List<Element> classProList = classInfo.elements("property");
        for (int i = 0; i < classProList.size(); i++) {
            Element jdbcPro = classProList.get(i);
            tableMap.put(jdbcPro.elementText("name"), jdbcPro.elementText("column"));
        }
        return tableMap;
    }
}
