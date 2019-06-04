package com.yb.shiro.server.shiro.realm;

import com.yb.common.server.dic.UserConstant;
import com.yb.common.server.other.LoginUser;
import com.yb.common.server.utils.JwtTool;
import com.yb.shiro.server.shiro.response.JwtToken;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 自定义的授权和认证
 *
 * @author biaoyang
 */
@Slf4j
@Service
@AllArgsConstructor
public class MyRealm extends AuthorizingRealm {
    private static final String COMMA = ".";
    private final RedisTemplate<String,Serializable> redisTemplate;
    private static final String AUTHORIZATION_HEADER = "Bearer ";

    /**
     * 大坑!  必须重写此方法,不然Shiro会报错
     * 虽然我用的时候没写这个也没有报错,但是最好加上吧
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    /**
     * 授权,获取用户角色或权限,封装到SimpleAuthorizationInfo里返回给shiro处理
     *
     * @param principals
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        //因为能进入这里的就只有过滤器那里的那个登录,那个传过来的参数是不会为空的
        String subject = principals.toString();
        //判断是用户名密码的登录还是token的认证登录
        if (subject.startsWith(AUTHORIZATION_HEADER) && subject.contains(COMMA)) {
            //解析token信息
            LoginUser loginUser = JwtTool.checkAndGetLoginUser(subject);
            //判断解析的荷载是否存在,不存在则登录验证失败(设定荷载里存储的是用户的不敏感但是需要用的信息)
            if (Objects.isNull(loginUser)) {
                throw new UnauthorizedException("授权失败");
            }
            //封装角色和权限
            SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
            //注意这里添加的角色和权限不能是null,某则包空指针异常(实测)
            simpleAuthorizationInfo.addRoles(CollectionUtils.isNotEmpty(loginUser.getRoles()) ? loginUser.getRoles() : Collections.emptySet());
            simpleAuthorizationInfo.addStringPermissions(CollectionUtils.isNotEmpty(loginUser.getPerms()) ? loginUser.getPerms() : Collections.emptySet());

            return simpleAuthorizationInfo;
        } else {
            throw new UnauthorizedException("授权失败");
        }
    }

    /**
     * 认证登录信息,这里的密码和principals都是token
     *
     * @param auth
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) throws AuthenticationException {
        //因为能进入这里的就只有过滤器那里的那个登录,那个传过来的参数是不会为空的
        String token = (String) auth.getCredentials();
        // 解密获得username，用于和数据库进行对比
        LoginUser loginUser = JwtTool.checkAndGetLoginUser(token);
        if (Objects.isNull(loginUser)) {
            throw new UnauthorizedException("认证失败");
        }
        //查询token的jti是否还有效
        String jti = (String) Optional.ofNullable(redisTemplate.opsForValue().get(UserConstant.USER_SYSTEM + loginUser.getUsername()))
                .orElseThrow(() -> new UnauthorizedException("无效的token信息"));
        //之所以在这里校验jti是否存在,是因为不想在过滤器那里处理,没有把过滤器实例化为bean,无法注入redis使用
        if (StringUtils.isBlank(jti)) {
            //防止这里jti是空字符串的情况(应该不会出现,但是有备无患)
            log.info("token的jti已经失效");
            throw new UnauthorizedException("认证失败");
        }
        //myRealm这个可以随便写,以前的做法是空字符串,null是不能通过的,源码不让等于null
        return new SimpleAuthenticationInfo(token, token, "myRealm");
    }
}

