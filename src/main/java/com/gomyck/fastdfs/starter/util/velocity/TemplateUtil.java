package com.gomyck.fastdfs.starter.util.velocity;

import com.gomyck.util.ObjectJudge;
import lombok.Data;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

/*
 * EXP:
 * List<TemplateUtil.KVEntity> list = new ArrayList<>();
 * list.add(TemplateUtil.initKVEntity("cplx", "你好 123123######..."));
 * list.add(TemplateUtil.initKVEntity("xmmc", "hello"));
 * list.add(TemplateUtil.initKVEntity("bhsl", "13#$%<><%?>$#?."));
 * byte[] inject = TemplateUtil.inject("ready.xml.vm", list);
 *
 * try {
 *     FileOutputStream fos = new FileOutputStream("demo.xls");
 *     fos.write(inject);
 *     fos.flush();
 *     fos.close();
 * } catch (Exception e) {
 *     log.error(CkLogger.getTrace(e));
 * }
 *
 */

/**
 * 模版填充工具类
 * <p>
 * 可以填充 excel word 等文件
 * <p>
 * 使用方法: 编辑 excel|word 模版, 把需要填充的单元格写入占位符: ${xxx}
 * <p>
 * 保存模版为.xml 格式, 修改文件名为: demo.xml.vm
 * <p>
 * 保存文件到 classpath 下, 否则默认的模版引擎获取不到
 * <p>
 * excel 导出的 xml 需要注意把 xml 中的
 * <p>
 * &lt;NumberFormat ss:Format="_ * #,##0_ ;_ * \-#,##0_ ;_ * "-"_ ;_ @_ "/ &gt;  删除
 * <p>
 * word 文件导出, 如果存在多条遍历的情况, 那么就找对应循环行的 tr 开头的标签, 写 foreach 就可以
 * <p>
 * 整行删除, 否则导出的文件为空
 *
 * @author gomyck
 * @version 1.0.0
 * @since 2020-03-17
 */
public class TemplateUtil {

    private TemplateUtil() {
    }

    private final static VelocityEngine ve = new VelocityEngine();

    private static boolean initialized = false;

    /**
     * 制定引擎
     *
     * @param prop 配置信息
     *
     * @return 模版引擎实例
     */
    public static VelocityEngine init(Properties prop) {
        VelocityEngine _ve = new VelocityEngine();
        if (prop == null) {
            throw new RuntimeException("you can use simpleInject if you don`t want to input the parameter [prop]");
        } else {
            _ve.init(prop);
        }
        return _ve;
    }

    /**
     * 转换方法, 通常使用该方法即可将数据注入模版
     *
     * @param templateFilePath 模版所在路径
     * @param injectData       注入的数据
     * @param ve               引擎对象
     *
     * @return 填充之后的模版
     */
    public static byte[] inject(String templateFilePath, List<KVEntity> injectData, VelocityEngine ve) {
        VelocityContext context = initVelocityContext(injectData);
        Template template = ve.getTemplate(templateFilePath, StandardCharsets.UTF_8.toString());
        StringWriter sw = new StringWriter();
        template.merge(context, sw);
        return sw.toString().getBytes();
    }


    /**
     * 转换方法, 使用 classpathResourceLoader 读取模版信息, 不需要使用 init 方法初始化引擎
     *
     * @param templateFilePath 模版所在路径(默认使用 classpath resource loader 检索, 如果是磁盘位置, 则需要使用 init 方法制定模版引擎)
     * @param injectData       注入的数据
     *
     * @return 填充之后的模版
     */
    public static byte[] simpleInject(String templateFilePath, List<KVEntity> injectData) {
        return inject(templateFilePath, injectData ,getVE());
    }

    /**
     * 模版填充
     *
     * @param templateString 模版数据(字符串) 一般来说  是 excel 模版, 使用 ExcelUtil 工具类, 能获取到 excel 中的模版信息
     *                       我曾经尝试过把 excel 导出为 xml 格式的文件, 可以填充模版, 但是在还原会 xls 或 xlsx 时, POI 并不
     *                       能读取这个文件, 原因是因为 POI 不支持, 具体是因为, 文件头是 xml 格式, POI 直接抛异常了
     *                       所以使用 ExcelUtil 工具类, 可以提取 xlsx 的共享字符串, 即代表内容的 xml, 然后塞给模版引擎, 填充
     *                       之后, 再放回到 excel 对象中, 实现内容替换
     * @param injectData 注入的数据
     * @return 填充之后的模版
     */
    public static byte[] inject(String templateString, List<KVEntity> injectData){
        Properties prop = new Properties();
        prop.put("resource.loader","ckStringResourceLoader");
        prop.put("ckStringResourceLoader.resource.loader.class", "com.gomyck.util.velocity.CkStringResourceLoader");
        VelocityContext context = initVelocityContext(injectData);
        VelocityEngine ve = TemplateUtil.init(prop);
        Template template = ve.getTemplate(templateString, StandardCharsets.UTF_8.toString());
        StringWriter sw = new StringWriter();
        template.merge(context, sw);
        return sw.toString().getBytes();
    }

    /**
     * 初始化 velocity 上下文
     *
     * @param injectData 注入的数据
     * @return velocity 上下文
     */
    private static VelocityContext initVelocityContext(List<KVEntity> injectData) {
        VelocityContext context = new VelocityContext();
        injectData.forEach(e -> {
            if (ObjectJudge.notNull(e.getKey())) {
                context.put(e.getKey(), e.getValue());
            } else {
                context.put(e.getKey(), "");
            }
        });
        return context;
    }

    public static KVEntity initKVEntity(String key, Object value) {
        return new KVEntity(key, value);
    }


    private static VelocityEngine getVE() {
        //init 已同步, 不需要在同步
        if (!initialized) {
            initialized = true;
            Properties prop = new Properties();
            prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            ve.init(prop);
        }
        return ve;
    }

    @Data
    public static class KVEntity {

        KVEntity(String key, Object value) {
            this.key = key;
            if (value instanceof String) {
                this.value = desensitization((String) value);
            } else {
                this.value = value;
            }
        }

        private String key;

        private Object value;

    }

    private static String desensitization(String param) {
        return param.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }


}
