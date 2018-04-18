package cn.weicm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by weicm on 2018/4/17.
 */
public class Main {
    static {
        ULog4j.configure(null, System.getProperty("user.dir")+"/log-util/");
    }
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        log.trace("This is a trace msg!");
        log.debug("This is a debug msg!");
        log.info("This is a info msg!");
        log.warn("This is a warn msg!");
        log.error("This is a ERROR msg!");
    }
}
