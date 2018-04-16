package cn.weicm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * <p>Created by weicm on 2018/3/26 10:43</p>
 * <p>Desp: 配置工具类</p>
 * <p>&nbsp;&nbsp;&nbsp;&nbsp; 用来加载 .properties 类型的配置文件，并提供属性访问功能</p>
 * <p>&nbsp;&nbsp;&nbsp;&nbsp; 者获取 classpath 下的文件绝对路径</p>
 * <p>&nbsp;&nbsp;&nbsp;&nbsp; 读取指定绝对路径的文件</p>
 */
public final class Conf {
    private static final Logger log = LoggerFactory.getLogger(Conf.class);
    //当前工程的类的字节码对象,用来从jar包内加载当前工程的配置
    private static Class clazz;
    private static final Properties prop = new Properties();
    private static final CountDownLatch latch = new CountDownLatch(1);
    private static final long WAIT_MAX_SECONDES = 5;

    private Conf() {
    }

    /**
     * <p>Created by weicm on 2018/3/26 10:43</p>
     * <p>Desp: 从当前工程中初始化指定配置文件</p>
     * <p>&nbsp;&nbsp;&nbsp;&nbsp; 默认从classpath寻找配置文件，classpath没有则从jar包内查找</p>
     * @param clazz 当前工程中的Class字节码对象
     * @param files 要加载的配置文件
     */
    public static void init(Class clazz, String... files) {
        Conf.clazz = clazz;
        try {
            for (int i = 0; i < files.length; i++) {
                String filePath = files[i].trim();
                InputStream in = null;
                try {
                    in = getResourceAsStream(clazz, filePath);
                    prop.load(in);
                } catch (Exception e) {
                    throw new RuntimeException("Init Conf util occur exception!", e);
                } finally {
                    try {
                        in.close();
                    } catch (Exception e) {
                        log.error("Close inputstream occur exception!", e);
                    }
                }
            }
        } finally {
            latch.countDown();
        }
    }

    /**
     * <p>Created by weicm on 2018/3/26 10:43</p>
     * <p>Desp: 从配置中获取key的值</p>
     * <p>&nbsp;&nbsp;&nbsp;&nbsp; 前提：必须初始化Conf(即:调用init方法)</p>
     * @param key
     * @return
     */
    public static String get(String key) {
        try {
            latch.await(WAIT_MAX_SECONDES, TimeUnit.SECONDS);
            if (latch.getCount() > 0)
                throw new RuntimeException("Conf init timeout or doesn't init!");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return prop.getProperty(key);
    }

    /**
     * <p>Created by weicm on 2018/3/26 10:43</p>
     * <p>Desp: 从配置中获取指定key的值,如果key-value不存在,则返回默认值defaultValue</p>
     * <p>&nbsp;&nbsp;&nbsp;&nbsp; 前提：必须初始化Conf(即:调用init方法)</p>
     * @param key
     * @param defaultValue
     * @return
     */
    public static String get(String key, String defaultValue) {
        try {
            latch.await(WAIT_MAX_SECONDES, TimeUnit.SECONDS);
            if (latch.getCount() > 0)
                throw new RuntimeException("Conf init timeout or doesn't init!");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return prop.getProperty(key, defaultValue);
    }

    /**
     * <p>Created by weicm on 2018/3/26 11:12</p>
     * <p>Desp: 从配置中获取所有的键</p>
     * <p>&nbsp;&nbsp;&nbsp;&nbsp; 前提：必须初始化Conf(即:调用init方法)</p>
     * @return 所有键
     */
    public static List<String> keys() {
        try {
            latch.await(WAIT_MAX_SECONDES, TimeUnit.SECONDS);
            if (latch.getCount() > 0)
                throw new RuntimeException("Conf init timeout or doesn't init!");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<String> listKeys = new ArrayList<>(prop.size());
        Iterator<Object> it = prop.keySet().iterator();
        while (it.hasNext()) {
            listKeys.add(it.next().toString());
        }
        return listKeys;
    }

    /**
     * <p>Created by weicm on 2018/3/26 11:12</p>
     * <p>Desp: 从配置中获取所有的值</p>
     * <p>&nbsp;&nbsp;&nbsp;&nbsp; 前提：必须初始化Conf(即:调用init方法)</p>
     * @return 所有值
     */
    public static List<String> values() {
        try {
            latch.await(WAIT_MAX_SECONDES, TimeUnit.SECONDS);
            if (latch.getCount() > 0)
                throw new RuntimeException("Conf init timeout or doesn't init!");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<String> listValues = new ArrayList<>(prop.size());
        Iterator<Object> it = prop.values().iterator();
        while (it.hasNext()) {
            listValues.add(it.next().toString());
        }
        return listValues;
    }

    /**
     * <p>Created by weicm on 2018/3/26 10:43</p>
     * <p>Desp: 从当前工程中初始化指定配置文件</p>
     * <p>&nbsp;&nbsp;&nbsp;&nbsp; 默认从classpath寻找配置文件，classpath没有则从jar包内查找</p>
     * <p>&nbsp;&nbsp;&nbsp;&nbsp; 前提：必须初始化Conf(即:调用init方法)</p>
     * @param file 要加载的配置文件
     * @return 是否加载成功，成功：true，失败：false
     */
    public static boolean load(String file) {
        try {
            latch.await(WAIT_MAX_SECONDES, TimeUnit.SECONDS);
            if (latch.getCount() > 0)
                throw new RuntimeException("Conf init timeout or doesn't init!");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        InputStream in = getResourceAsStream(clazz, file);
        try {
            prop.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if(null != in)
                    in.close();
            } catch (IOException e) {
                log.error("Close inputstream occur exception!", e);
                return false;
            }
        }
        return true;
    }

    /**
     * <p>Created by weicm on 2018/3/26 10:43</p>
     * <p>Desp: 获取当前工程指定配置文件对应的Properties对象</p>
     * <p>&nbsp;&nbsp;&nbsp;&nbsp; 前提：必须初始化Conf(即:调用init方法)</p>
     * @param file
     * @return
     */
    public static Properties newProperties(String file) throws Exception {
        try {
            latch.await(WAIT_MAX_SECONDES, TimeUnit.SECONDS);
            if (latch.getCount() > 0)
                throw new RuntimeException("Conf init timeout or doesn't init!");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        InputStream in = getResourceAsStream(clazz, file);
        Properties pp = new Properties();
        try {
            pp.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (null != in)
                    in.close();
            } catch (IOException e) {
                log.error("Close inputstream occur exception!", e);
            }
        }
        return pp;
    }

    /**
     * <p>Created by weicm on 2018/4/3 11:23</p>
     * <p>Desp: 获取当前工程指定配置文件对应的LinkedHashMap对象</p>
     * <p>&nbsp;&nbsp;&nbsp;&nbsp; 前提：必须初始化Conf(即:调用init方法)</p>
     * @param file 指定配置文件
     * @return 有序的属性（LinkedHashMap）
     * @throws Exception
     */
    public static Map<String,String> newLinkedProperties(String file) throws Exception{
        latch.await(WAIT_MAX_SECONDES, TimeUnit.SECONDS);
        if (latch.getCount() > 0)
            throw new RuntimeException("Conf init timeout or doesn't init!");
        InputStream in = getResourceAsStream(clazz, file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        LinkedHashMap<String, String> linkedProperties = new LinkedHashMap<>();
        String line = null;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.equals("") || line.startsWith("#") || line.startsWith("="))
                continue;
            int index = line.indexOf("=");
            if (index > 0) {
                String k = line.substring(0, index);
                String v = index < line.length() - 1 ? line.substring(index + 1) : "";
                linkedProperties.put(k, v);
            }else {
                linkedProperties.put(line, "");
            }
        }
        return linkedProperties;
    }

    /**
     * <p>Created by weicm on 2018/3/26 10:43</p>
     * <p>Desp: 从当前工程中加载文件输入流</p>
     * <p>&nbsp;&nbsp;&nbsp;&nbsp; classpath 或者 jar包内存在文件名为 file 的文件则正常返回，否则抛出异常</p>
     * <p>&nbsp;&nbsp;&nbsp;&nbsp; 有限搜索 classpath</p>
     * <p>&nbsp;&nbsp;&nbsp;&nbsp; 前提：必须初始化Conf(即:调用init方法)</p>
     * @param file  要读取的文件
     * @return 文件输入流
     */
    public static InputStream getResourceAsStream(Class clazz, String file) {
        /*
        Java 读取文件说明：
            //返回的是inputstream
            Main.class.getResourceAsStream ();
            //返回:URL
            Main.class.getResource();
            //返回的是当前Class这个类所在包的位置
            System.out.println(Main.class.getResource(""));
            //返回的是classpath的位置
            System.out.println(Main.class.getResource("/"));
            //返回的是classpath的位置
            System.out.println(Main.class.getClassLoader().getResource(""));
            //错误的 !!
            System.out.println(Main.class.getClassLoader().getResource("/"));
        */
        InputStream in = null;
        //从classpath路径查找文件
        String classPathFile = getAbsolutePath(file);
        try {
            if (null != classPathFile) {
                in = new FileInputStream(classPathFile);
                log.info("Finded file from classpath! path: " + classPathFile);
            }else {
                //classpath没有则从jar内查找文件
                file = file.startsWith("/") ? file : "/" + file;
                in = clazz.getResourceAsStream(file);
                if (null != in) {
                    log.info("Finded file from jar! path: " + clazz.getResource(file).getPath());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (null == in) {
            System.out.println("File doesn't exists! file: " + file);
            throw new RuntimeException("File doesn't exists! file: " + file);
        }
        return in;
    }

    /**
     * <p>Created by weicm on 2018/3/26 10:51</p>
     * <p>Desp: 从classpath（包括classpath路径中的jar所在目录）路径下搜索文件名为 fileName 的文件</p>
     * @param fileName 文件名
     * @return 找到则返回文件绝对路径，找不到则返回null
     */
    public static String getAbsolutePath(String fileName) {
        String[] classPathes = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
        String fileAbsolutePath = findFile(fileName, classPathes);
        return fileAbsolutePath;
    }

    /**
     * <p>Created by weicm on 2018/3/26 10:51</p>
     * <p>Desp: 从指定路径 searchPathes 查找文件名为 fileName 的文件</p>
     * @param fileName 文件名
     * @param searchPathes 搜索路径
     * @return 找到则返回文件绝对路径，找不到则返回null
     */
    public static String findFile(String fileName, String... searchPathes) {
        Set<File> libDirs = new LinkedHashSet<>();
        for (int i = 0; i < searchPathes.length; i++) {
            String path = searchPathes[i].trim();
            File curFile = new File(path).getAbsoluteFile();
            if (curFile.exists()) {
                if (curFile.isFile()) {
                    if (curFile.getName().equals(fileName)) {
                        return curFile.getPath();
                    }
                    libDirs.add(curFile.getParentFile().getAbsoluteFile());
                } else {
                    if (Arrays.asList(curFile.list()).contains(fileName)) {
                        return new File(curFile, fileName).getPath();
                    }
                }
            }
        }
        for (File libDri: libDirs) {
            if (Arrays.asList(libDri.list()).contains(fileName)) {
                return new File(libDri, fileName).getPath();
            }
        }
        return null;
    }
}