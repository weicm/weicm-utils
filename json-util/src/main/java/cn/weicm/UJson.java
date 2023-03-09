package cn.weicm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * <p>Author: weicm</p>
 * <p>Date: 2018/1/26 14:03</p>
 * <p>Desp: 依赖于Jackson 和 JsonPath 的 json 处理工具；线程安全；</p>
 */
public class UJson {
    static {
        //设置默认解析器为Jackson
        Configuration.setDefaults(new Configuration.Defaults() {
            private final JsonProvider jsonProvider = new JacksonJsonProvider(
                    new ObjectMapper()
                            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false));
            private final MappingProvider mappingProvider = new JacksonMappingProvider(new ObjectMapper()
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false));

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                /*
                 * 设置解析行为：压制异常，计算path表达式路径时出现异常时，返回null
                 */
                return EnumSet.of(Option.SUPPRESS_EXCEPTIONS);
            }
        });
    }

    public static Boolean isValid(String jsonStr) {
        try {
            Object o = JsonPath.parse(jsonStr).json();
            return o instanceof List || o instanceof Map;
        } catch (Exception e) {
            return false;
        }
    }
    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/1/26 14:00</p>
     * <p>Desp: 从指定json字符串根据指定path表达式和Class类型解析数据</p>
     * <p>&nbsp;&nbsp;&nbsp;&nbsp; 注意，该方法不支持泛型</p>
     *
     * @param json  json字符串
     * @param path  path表达式
     * @param clazz 结果字节码类对象
     * @param <T>   结果泛型
     * @return 解析结果
     */
    public static <T> T parse(String json, String path, Class<T> clazz) {
        if (null == json || "".equals(json.trim())) {
            return null;
        }
        return JsonPath.parse(json).read(path, clazz);
    }

    public static <T> T parse(String json, Class<T> clazz) {
        return parse(json, "$", clazz);
    }

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/1/26 14:00</p>
     * <p>Desp: 从指定json字符串根据指定path表达式和泛型类型解析数据</p>
     * <p>&nbsp;&nbsp;&nbsp;&nbsp; 注意，该方法适合所有情况，推荐使用此方法！</p>
     *
     * @param json json字符串
     * @param path path表达式
     * @param type 泛型引用对象
     * @param <T>  结果泛型
     * @return 解析结果
     */
    public static <T> T parse(String json, String path, TypeRef<T> type) {
        if (null == json || "".equals(json.trim())) {
            return null;
        }
        return JsonPath.parse(json).read(path, type);
    }

    public static <T> T parse(String json, TypeRef<T> type) {
        return parse(json, "$", type);
    }

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/1/26 14:00</p>
     * <p>Desp: 将对象转换为json字符串</p>
     *
     * @param o 对象
     * @return json字符串
     */
    public static String str(Object o) {
        if (null == o) {
            return null;
        }
        return JsonPath.parse(o).jsonString();
    }

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/1/26 14:00</p>
     * <p>Desp: 编译json对象</p>
     * <p>&nbsp;&nbsp;&nbsp;&nbsp; 使用场景：为了达到提高解析速度的目的，通常配合方法 T parse(Object doc, String path) 一起使用</p>
     *
     * @param json
     * @return
     */
    public static Object compile(String json) {
        return Configuration.defaultConfiguration().jsonProvider().parse(json);
    }

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/1/26 13:59</p>
     * <p>Desp: 从编译过的json对象解析数据</p>
     *
     * @param doc  编译过的json上下文，支持类型：List, Map, Integer, Double, String, Boolean
     * @param path path表达式
     * @param <T>  解析后的类型
     * @return 解析结果
     */
    public static <T> T parse(Object doc, String path) {
        if (null == doc) {
            return null;
        }
        return JsonPath.read(doc, path);
    }

    /**
     * <p>Created by weicm on 2018/3/26 17:38</p>
     * <p>Desp: 先编译json字符串，后解析数据，解析类型通过返回类型指定</p>
     * <p>&nbsp;&nbsp;&nbsp;&nbsp; 应用场景：明确知道解析出的返回类型，如果类型不匹配容易出错！</p>
     *
     * @param json 要解析的json字符串
     * @param path jsonpath表达式
     * @param <T>  解析数据的类型
     * @return 解析后的数据，支持类型：List, Map, Integer, Double, String, Boolean
     */
    public static <T> T parse(String json, String path) {
        return parse(compile(json), path);
    }

    public static <K, V> Bean bean() {
        return new Bean<K, V>();
    }

    /**
     * 键值对对象构造器
     *
     * @param <K> 建的类型
     * @param <V> 值的类型
     */
    public static class Bean<K, V> {
        private O o = new O<K, V>();

        private Bean() {
        }

        /**
         * <p>Author: weicm</p>
         * <p>Date: 2018/1/26 13:53</p>
         * <p>Desp: 添加新的属性</p>
         *
         * @param key   键
         * @param value 值
         * @return 返回当前构造器
         */
        public Bean put(K key, V value) {
            o.put(key, value);
            return this;
        }

        /**
         * 构造键值对对象
         *
         * @return 键值对对象
         */
        public O<K, V> build() {
            return o;
        }
    }

    /**
     * 键值对对象
     *
     * @param <K> 键类型
     * @param <V> 值类型
     */
    public static class O<K, V> extends LinkedHashMap<K, V> {
        /**
         * JSON化格式的toString
         *
         * @return
         */
        @Override
        public String toString() {
            return str(this);
        }

        /**
         * <p>Author: weicm</p>
         * <p>Date: 2018/1/26 13:51</p>
         * <p>Desp: 将新的值放在location位置，原来的值放在容器最后</p>
         *
         * @param location 位置
         * @param key      新key
         * @param value    新value
         * @return 新value
         */
        public V replacePut(K location, K key, V value) {
            V removeValue = remove(location);
            V newValue = put(key, value);
            put(location, removeValue);
            return newValue;
        }
    }

    /**
     * <p>Created by weicm on 2018/3/30 14:55</p>
     * <p>Desp: 格式化json字符串</p>
     *
     * @param json 未格式化的JSON字符串
     * @return 格式化的JSON字符串。
     */
    public static String format(String json, String... excludeFileds) {
        //return format(json, "    ", true);
        Object o = parse(json, "$", Object.class);
        return format(o, "    ", Arrays.asList(excludeFileds), true);
    }


    /**
     * <p>Created by weicm on 2018/3/30 14:55</p>
     * <p>Desp: 将对象格式化json字符串</p>
     *
     * @param o       要被格式化的对象
     * @param space   格式化用的字符
     * @param exclude 属性值不需要继续格式化的属性对应的键
     * @param newLine 属性值格式化时是否需要换行
     * @return 格式化后的字符串
     */
    private static String format(Object o, final String space, List<String> exclude, boolean newLine) {
        String closeSpace = space.length() == 4 || !newLine ? "" : space.substring(0, space.length() / 2);
        String newLineTag = newLine ? "\n" : "";
        String spaceTag = newLine ? space : "";
        if (o instanceof Map) {
            StringBuilder mapSb = new StringBuilder();
            mapSb.append("{").append(newLineTag);
            Map map = (Map) o;
            map.forEach((key, value) -> {
                mapSb
                        .append(spaceTag)
                        .append("\"")
                        .append(key)
                        .append("\"")
                        .append(": ")
                        .append(format(value, space + space, exclude, exclude.contains(key) ? false : true))
                        .append(", ")
                        .append(newLineTag);
            });
            if (map.size() > 0) {
                mapSb.deleteCharAt(mapSb.lastIndexOf(","));
            }
            mapSb.append(closeSpace).append("}");
            return mapSb.toString();
        } else if (o instanceof List) {
            StringBuilder listSb = new StringBuilder();
            listSb.append("[").append(newLineTag);
            List list = (List) o;
            list.forEach(item -> {
                listSb
                        .append(spaceTag)
                        .append(format(item, space + space, exclude, true))
                        .append(", ")
                        .append(newLineTag);
            });
            if (list.size() > 0) {
                listSb.deleteCharAt(listSb.lastIndexOf(","));
            }
            listSb.append(closeSpace).append("]");
            return listSb.toString();
        } else if (o instanceof String) {
            return "\"" + o + "\"";
        } else {
            return String.valueOf(o);
        }
    }

    /**
     * 格式化json字符串，指定缩进
     *
     * @param json  未格式化的JSON字符串。
     * @param space 缩进字符串
     * @return 格式化的JSON字符串。
     * @commaNewLine 逗号是否换行
     */
    public static String format(String json, String space, boolean commaNewLine) {
        StringBuffer result = new StringBuffer();
        int length = json.length();
        int number = 0;
        char key = 0;

        //遍历输入字符串。
        for (int i = 0; i < length; i++) {
            //1、获取当前字符。
            key = json.charAt(i);
            //如果key为冒号
            if (key == ':' && json.charAt(i - 1) == '"') {
                result.append(key + " ");
                continue;
            }
            //2、如果当前字符是前方括号、前花括号做如下处理：
            if ((key == '[') || (key == '{')) {
                //（1）打印：当前字符。
                result.append(key);
                //（2）前方括号、前花括号，的后面必须换行。打印：换行。
                if (!(key == '[' && json.charAt(i + 1) == ']') && !(key == '{' && json.charAt(i + 1) == '}')) {
                    result.append('\n');
                    //（3）每出现一次前方括号、前花括号，并且不是空括号时，缩进次数增加一次。打印：新行缩进。
                    number++;
                    result.append(indent(space, number));
                }
                //（4）进行下一次循环。
                continue;
            }
            //3、如果当前字符是后方括号、后花括号做如下处理：
            if ((key == ']') || (key == '}')) {
                //（1）后方括号、后花括号，的前面必须换行。打印：换行。
                if (!(key == ']' && json.charAt(i - 1) == '[') && !(key == '}' && json.charAt(i - 1) == '{')) {
                    result.append('\n');
                    //（2）每出现一次后方括号、后花括号；缩进次数减少一次。打印：缩进。
                    number--;
                    result.append(indent(space, number));
                }
                //（3）打印：当前字符。
                result.append(key);
                //（4）继续下一次循环。
                continue;
            }

            //4、如果当前字符是逗号。逗号后面换行，并缩进，不改变缩进次数。
            if ((key == ',')) {
                result.append(key);
                if ((json.charAt(i - 1) == ']' || json.charAt(i - 1) == '}') || commaNewLine) {
                    result.append('\n');
                    result.append(indent(space, number));
                } else
                    result.append(' ');
                continue;
            }
            //5、打印：当前字符。
            result.append(key);
        }
        return result.toString();
    }

    /**
     * 返回指定次数的缩进字符串。每一次缩进三个空格，即SPACE。
     *
     * @param number 缩进次数。
     * @return 指定缩进次数的字符串。
     */
    private static String indent(String indent, int number) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < number; i++) {
            result.append(indent);
        }
        return result.toString();
    }
}
