package com.yb.shiro.server.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 * @author biaoyang
 * date 2019/4/25 002515:18
 */
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "permit.config")
public class ApplicationPermitConfig {
    /**不需要登录的uri/url */
    private String[] permitUrls;
    /**token的加密秘钥 */
    private String base64Secret;

}
