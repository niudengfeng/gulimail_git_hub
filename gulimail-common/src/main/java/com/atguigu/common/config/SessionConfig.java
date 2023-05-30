package com.atguigu.common.config;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import com.alibaba.fastjson.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.FlashMap;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * 解决两件事：
 *  1.存放redis数据序列化为json
 *  2.解决跨域session共享
 */
@Configuration
public class SessionConfig {

    //json无法解析的包名
    public static Set<String> packeage = new HashSet<>();


    static {
        packeage.add("org.springframework.web.servlet.FlashMap");
    }

    @Bean
    public CookieSerializer cookieSerializer(){
        DefaultCookieSerializer defaultCookieSerializer = new DefaultCookieSerializer();
        defaultCookieSerializer.setDomainName("gulimail.com");
//        defaultCookieSerializer.setDomainNamePattern("*.gulimail.com");
        return defaultCookieSerializer;
    }

    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer(){
        return new MyGenericFastJsonRedisSerializer();
    }

    public class MyGenericFastJsonRedisSerializer extends GenericFastJsonRedisSerializer{
        @Override
        public Object deserialize(byte[] bytes) throws SerializationException {
            ParserConfig parserConfig = new ParserConfig();
            parserConfig.setAutoTypeSupport(true);
            if (bytes != null && bytes.length != 0) {
                try {
                    String input = new String(bytes, IOUtils.UTF8);
                    if (!StringUtils.isEmpty(input)){
                        boolean f = false;
                        for (String s : packeage) {
                            //目前仅支持 处理FLASHMAP
                            List<FlashMap> flashMaps = new ArrayList<>();
                            if (input.contains(s)){
                                JSONArray array = JSONUtil.parseArray(input);
                                if (array ==null){
                                    return array;
                                }
                                for (Object o : array) {
                                    JSONObject jsonObject = JSONUtil.parseObj(o);
                                    jsonObject.remove("@type");
                                    for (Map.Entry<String, Object> stringObjectEntry : jsonObject.entrySet()) {
                                        String key = stringObjectEntry.getKey();
                                        Object value = stringObjectEntry.getValue();
                                        FlashMap flashMap = new FlashMap();
                                        flashMap.put(key,value);
                                        flashMaps.add(flashMap);
                                    }
                                }
                                f = true;
                            }
                            if (f){
                                return flashMaps;
                            }
                        }
                    }
                    Object o = JSON.parseObject(input, Object.class, parserConfig, new Feature[0]);
                    return o;
                } catch (Exception var3) {
                    throw new SerializationException("Could not deserialize: " + var3.getMessage(), var3);
                }
            } else {
                return null;
            }
        }
    }

}
