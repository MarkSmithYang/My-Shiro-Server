package com.yb.shiro.server.shiro.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.shiro.authc.AuthenticationToken;

/**
 * 返回token信息封装类,也是实现AuthenticationToken的类,
 * 用这个来传递token到shiro的realm里认证和授权
 * @author biaoyang
 */
@Data
@ApiModel(description = "JwtToken封装类")
public class JwtToken implements AuthenticationToken {

    @ApiModelProperty("token类型")
    private String tokenType;
    @ApiModelProperty("访问用token")
    private String accessToken;
    @ApiModelProperty("刷新用token")
    private String refreshToken;
    @ApiModelProperty("过期时间(秒)")
    private long expiresSeconds;

    /**
     * 构造的时候直接把token放进去,然后用这个信息去登录
     * 然后在认证和授权的方法里处理逻辑
     * @param accessToken
     */
    public JwtToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public JwtToken(String accessToken, String refreshToken,
                    String tokenType, long expiresSeconds) {
        this.tokenType = tokenType;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresSeconds = expiresSeconds;
    }

    @JsonIgnore
    @Override
    public Object getPrincipal() {
        return accessToken;
    }

    @JsonIgnore
    @Override
    public Object getCredentials() {
        return accessToken;
    }
}


