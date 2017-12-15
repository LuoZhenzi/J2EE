package sc.ustc.controller;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.*;

public class SimpleController extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writer.print("<html><head><title>SimpleController</title></head><body>欢迎使用SimpleController!</body></html>");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}