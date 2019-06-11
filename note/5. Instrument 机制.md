
## Instrument 机制
监控代码的运行，如何在不侵入原生代码的情况下提供一套有效的监控机制，JDK 5 中引入 instrument 机制，
* 基本过程：
    * 实现一个 Agent 和 Transformer，打包成 myAgent.jar
    * java -javaagent:myAgent.jar Test

#### 示例

##### Fruit 定义一个水果类
```java
public class Fruit {
public String whoami(){
    return "apple";
}
}

```

##### FruitTransformer 定义一个水果转换器
上面的水果是 apple，重新编写一个 orange 的水果类并生成新的 Fruit.class，
```java
package asm;
public class FruitTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        byte[] bytes = null;
        FileChannel rf = null;
        if ("asm/Fruit".equals(className)){
            System.out.println("fruit is checking...");
            try {
                rf = new RandomAccessFile("Fruit.class","rw").getChannel();//加载 orange 的水果类，实现字节码的更改
                int size = (int)rf.size();
                bytes = new byte[size];
                rf.read(ByteBuffer.wrap(bytes));
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    rf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bytes;
    }
}
```

##### FruitAgent 实现 premain 方法，加载水果转化器
JVM 在类加载时候会先执行 Agent 的 premain 方法，再执行 Java 的 main 方法
```java
public class FruitAgent {
    public static void premain(String options, Instrumentation ins) {
        if (options!=null){
            System.out.println("calling with "+ options);
        }else {
            System.out.println("no options");
        }
        ins.addTransformer(new FruitTransformer());
    }
}
```
##### Agent 和 Transformer 定义完成，打包成 jar 包
在 resources 下创建 META-INF/MANIFEST.MF 文件，内容如下
```
Manifest-Version: 1.0
Premain-Class: asm.FruitAgent
Can-Redefine-Classes: true

```
在 pom.xml 添加 build 代码块， mvn install 生成 xagent.jar
```maven
    <build>
        <finalName>xagent</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <configuration>
                    <archive>
                        <manifestFile>src/main/resources/META-INF/MANIFEST.MF</manifestFile>
                    </archive>
                </configuration>
            </plugin>
        </plugins>
    </build>
```
然后就可以在 IDEA 中，相应的测试类 `TestFruit.java`
```java
public class TestFruit {
    public static void main(String[] args) {
        System.out.println(new Fruit().whoami());
    }
}
```
添加 vm 参数，  ``-javaagent:target\xagent.jar=hihihi``, 其中 "hihihi" 是 #FruitAgent.premain 的参数 options

添加参数前的输出
```
apple
```
添加参数后的输出
```
calling with hihihi
fruit is checking...
orange
```

## agentmain
JDK5 的 instrument 机制，局限于 main 函数执行前，JDK 6 增加了 agentmain， 可以通过 vm attach 到正在运行的程序，修改其字节码和运行结果

##### FruitAgentMain 实现 agentmain 方法，加载水果转化器(同上)
```java
public class FruitAgentMain {
    public static void agentmain(String options, Instrumentation ins) throws UnmodifiableClassException, ClassNotFoundException {
        if (options!=null){
            System.out.println("calling with des "+ options);
        }else {
            System.out.println("no options");
        }
        ins.addTransformer(new FruitTransformer(), true);
        ins.retransformClasses(Fruit.class);
    }
}
```
##### 修改 MANIFEST.MF 文件，重新打包成 magent.jar
```
Manifest-Version: 1.0
Agent-Class: asm.FruitAgentMain
Can-Redefine-Classes: true
Can-Retransform-Classes: true
```


##### TestFruit 每 3 秒打印一次，先运行

```java
public class TestFruit {
    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 10000000; i++) {
            System.out.println("we get " + new Fruit().whoami() + " at "+i);
            TimeUnit.SECONDS.sleep(3);
        }
    }
}
```

##### AttachFruit 测试 agentmain 功能
首先获取 TestFruit 的进程 id，也可使用 jps，然后加载 magent.jar, 这里使用绝对路径。
```java
public class AttachFruit {
    public static void main(String[] args) throws IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {
        List<VirtualMachineDescriptor> list = VirtualMachine.list();
        for (VirtualMachineDescriptor vmd : list) {
            if (vmd.displayName().endsWith("TestFruit")) {
                System.out.println("coming "+ vmd.id());
                VirtualMachine virtualMachine = VirtualMachine.attach(vmd.id());
                virtualMachine.loadAgent("F:\\a_ja\\fruit\\target\\magent.jar", "hehe");
                System.out.println("==");
                virtualMachine.detach();
            }
        }
    }
}
```
##### 运行结果
TestFruit 程序独自运行时
```
we get apple at 0
we get apple at 1
we get apple at 2
...
```
AttachFruit 在其运行过程中 attach 进其进程后的结果
```
we get apple at 7
calling with des hehe
loading agentmain...
fruit is checking...
we get orange at 8
we get orange at 9
...
```
##### 遇到的问题

    * 只能 attach 一次，第二次加载修改后的 jar, 对程序无影响
    * 加载 magent.jar, 没有使用绝对路径 loadAgent 不成功
    * tools.jar 直接使用绝对路径设置 
    * 对于上面 orange 生成新的 Fruit.class， 使用绝对路径加载（不然加载不到）



