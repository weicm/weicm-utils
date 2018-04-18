package cn.weicm;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

import java.io.File;

/**
 * <p>Created by weicm on 2018/4/18 9:24</p>
 * <p>Desp: Log4j 配置工具类</p>
 * <p>&nbsp;&nbsp;&nbsp;&nbsp; 1. 指定配置文件</p>
 * <p>&nbsp;&nbsp;&nbsp;&nbsp; 2. 指定日志输出目录（如果有日志文件）</p>
 */
public class ULog4j {

    /**
     * <p>Created by weicm on 2018/4/18 9:25</p>
     * <p>Desp: 默认配置</p>
     * <p>&nbsp;&nbsp; 1. 配置文件使用 classpath 下的 log4j.xml 或 log4j.properties</p>
     * <p>&nbsp;&nbsp; 2. 日志输出目录与系统环境变量 user.dir 的值相同，配置文件中通过 ${log.dir} 使用该变量</p>
     */
    public static void configure() {
        configure(null, null);
    }

    /**
     * <p>Created by weicm on 2018/4/18 9:26</p>
     * <p>Desp: 定制化配置</p>
     * @param confFile 配置文件路径，如果为 null 或 空字符串，则为默认配置
     * @param logDir 日志输出目录，日志目录的系统变量 log.dir，配置文件中通过 ${log.dir} 使用该变量， 如果为 null 或 空字符串，则为默认配置
     */
    public static void configure(String confFile, String logDir) {
        //设置日志输出目录
        logDir = standardize(logDir);
        logDir = null == logDir || "".equals(logDir) ? System.getProperty("user.dir") : logDir.trim();
        logDir = logDir.endsWith("/") ? logDir.replaceFirst("/$", "") : logDir;
        System.setProperty("log.dir", logDir);

        //配置生效
        if (null != confFile && !"".endsWith(confFile)) {
            if (confFile.endsWith(".xml")) {
                DOMConfigurator.configure(confFile);
            } else if (confFile.endsWith(".properties")){
                PropertyConfigurator.configure(confFile);
            } else {
                throw new IllegalArgumentException("Log conf file format error! Just support *.xml and *.properties!");
            }
        } else {
            BasicConfigurator.configure();
        }
    }

    private static String standardize(String path) {
        if (File.separator.equals("/")) {
            return path.replaceAll("\\\\", "/");
        } else {
            return path.replaceAll("/", "\\\\");
        }
    }
}
