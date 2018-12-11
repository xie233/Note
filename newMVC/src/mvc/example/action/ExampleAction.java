package mvc.example.action;

import mvc.core.NPAutowired;
import mvc.core.NPController;
import mvc.core.NPRequestMapping;
import mvc.core.NPRequestParam;
import mvc.example.service.IDemoService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@NPController
@NPRequestMapping("/example")
public class ExampleAction {

    @NPAutowired
    private IDemoService demoService;

    @NPRequestMapping("/query")
    public void query(HttpServletRequest req,
                      HttpServletResponse resp,
                      @NPRequestParam("name") String name){
        String result = demoService.get(name);
        try{

            resp.getWriter().write(result);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @NPRequestMapping("/add")
    public void add(HttpServletRequest req,
                    HttpServletResponse resp,
                    @NPRequestParam("a") Integer a,
                    @NPRequestParam("b") Integer b){
        try{
            resp.getWriter().write(a+"+"+b+"="+(a+b));
        }catch (IOException e){
            e.printStackTrace();
        }
    }




}
