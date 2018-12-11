package mvc.core;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class NPDispatcherServlet extends HttpServlet{



    private static final String LOCATION = "contextConfig";

    private Properties p = new Properties();

    private List<String> classNames = new ArrayList<>();
    private Map<String,Object> ioc = new HashMap<>();
    private Map<String,Method> handleMapping = new HashMap<>();


    public NPDispatcherServlet() {
        super();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        doLoadConfig(config.getInitParameter(LOCATION));

        doScanner(p.getProperty("scanPackage"));

        doInstance();

        doAutowired();

        initHandlerMapping();

        System.out.println("mvc is init");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try{
            doDispatch(req,resp);
        }catch (Exception e){
            resp.getWriter().write("Your input is wrong");
        }
    }

    private void doDispatch(HttpServletRequest req,HttpServletResponse resp) throws Exception{
        if(this.handleMapping.isEmpty()){
            return;
        }
        String url = req.getRequestURI();
//        String contextPath = req.getContextPath();

        if(!this.handleMapping.containsKey(url)){
            req.getRequestDispatcher("index.jsp").forward(req,resp);
            resp.getWriter().write("404 NOT FOUND");
            return;
        }

        Method method = this.handleMapping.get(url);

        Class<?>[] parameterTypes = method.getParameterTypes();
        Map<String,String[]> parameterMap = req.getParameterMap();
        Object[] paramValues = new Object[parameterTypes.length];
        Iterator params = parameterMap.entrySet().iterator();
        for(int i=0;i<parameterTypes.length;i++){
            Class parameterType = parameterTypes[i];
            if(parameterType == HttpServletRequest.class){
                paramValues[i] = req;
                continue;
            }else if(parameterType==HttpServletResponse.class){
                paramValues[i] = resp;
                continue;
            }else if(parameterType==String.class){
                Map.Entry<String,String[]> param = (Map.Entry<String,String[]>)params.next();
                String value = Arrays.toString(param.getValue())
                        .replaceAll("\\[|\\]","")
                        .replaceAll(",\\s",",");
                paramValues[i] = value;

            }else if(parameterType==Integer.class){
                Map.Entry<String,String[]> param = (Map.Entry<String,String[]>)params.next();
                Integer value = Integer.valueOf(param.getValue()[0]);
                paramValues[i] = value;
            }
        }
        try{
            String beanName = lowerFirstCase(method.getDeclaringClass().getSimpleName());
            method.invoke(this.ioc.get(beanName),paramValues);
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    private void doLoadConfig(String location){

        try(InputStream fis = this.getClass().getClassLoader().getResourceAsStream(location)){
            p.load(fis);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void doScanner(String packageName){
        URL url = this.getClass().getClassLoader().getResource("/"+packageName.replaceAll("\\.","/"));
        File dir = new File(url.getFile());
        for(File file1:dir.listFiles()){
            if(file1.isDirectory()){
                doScanner(packageName+"."+file1.getName());
            }else {
                classNames.add(packageName+"."+file1.getName().replace(".class","").trim());
            }
        }
    }

    private String lowerFirstCase(String str){
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private void doInstance(){
        if(classNames.size()==0){
            return;
        }
        try{
            for(String className:classNames){
                Class<?> clazz = Class.forName(className);
                if(clazz.isAnnotationPresent(NPController.class)){
                    String beanName = lowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName,clazz.newInstance());
                }else if(clazz.isAnnotationPresent(NPService.class)){
                    NPService service = clazz.getAnnotation(NPService.class);
                    String beanName = service.value();
                    if(!"".equals(beanName.trim())){
                        ioc.put(beanName,clazz.newInstance());
                        continue;
                    }
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for(Class<?> i:interfaces){
                        ioc.put(i.getName(),clazz.newInstance());
                    }
                }else {
                    continue;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void doAutowired(){
        if(ioc.isEmpty()){
            return;
        }
        for(Map.Entry<String,Object> entry:ioc.entrySet()){
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for(Field field:fields){
                if(!field.isAnnotationPresent(NPAutowired.class)){
                    continue;
                }
                NPAutowired autowired = field.getAnnotation(NPAutowired.class);
                String beanName = autowired.value().trim();
                if("".equals(beanName)){
                    beanName = field.getType().getName();
                }
                field.setAccessible(true);
                try{
                    field.set(entry.getValue(),ioc.get(beanName));
                }catch (Exception e){
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }

    private void initHandlerMapping(){
        if(ioc.isEmpty()){
            return;
        }
        for(Map.Entry<String,Object> entry:ioc.entrySet()){
            Class<?> clazz = entry.getValue().getClass();
            if(!clazz.isAnnotationPresent(NPController.class)){
                continue;
            }
            String baseUrl = "";
            if(clazz.isAnnotationPresent(NPRequestMapping.class)){
                NPRequestMapping  requestMapping = clazz.getAnnotation(NPRequestMapping.class);
                baseUrl = requestMapping.value();
            }
            Method[] methods = clazz.getMethods();
            for(Method method:methods){
                if(!method.isAnnotationPresent(NPRequestMapping.class)){
                    continue;
                }
                NPRequestMapping requestMapping = method.getAnnotation(NPRequestMapping.class);
                String url = ("/"+baseUrl+"/"+requestMapping.value()).replaceAll("/+","/");
                handleMapping.put(url,method);
                System.out.println("mapped"+url+","+method);
            }
        }
    }
}
