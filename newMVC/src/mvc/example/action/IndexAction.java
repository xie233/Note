package mvc.example.action;


import mvc.core.NPController;
import mvc.core.NPRequestMapping;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;


@NPController
@NPRequestMapping("/")
public class IndexAction {
    @NPRequestMapping("/")
    public void index(HttpServletRequest req,
                      HttpServletResponse resp) throws IOException,ServletException{

//        resp.getWriter().write("Hello MVC");
        resp.setContentType("text/html;charset=UTF-8");
        // Allocate a output writer to write the response message into the network socket
        PrintWriter out = resp.getWriter();

        // Write the response message, in an HTML page
        try {
            out.println("<!DOCTYPE html>");
            out.println("<html><head>");
            out.println("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>");
            out.println("<title>Hello, World</title></head>");
            out.println("<body>");
            out.println("<h1>Hello, world!</h1>");  // says Hello
            // Echo client's request information
            out.println("<p>Request URI: " + req.getRequestURI() + "</p>");
            out.println("<p>Protocol: " + req.getProtocol() + "</p>");
            out.println("<p>PathInfo: " + req.getPathInfo() + "</p>");
            out.println("<p>Remote Address: " + req.getRemoteAddr() + "</p>");
            // Generate a random number upon each request
            out.println("<p>A Random Number: <strong>" + Math.random() + "</strong></p>");
            out.println("</body>");
            out.println("</html>");
        } finally {
            out.close();  // Always close the output writer
        }
    }


    }

