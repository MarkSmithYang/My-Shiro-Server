package com.yb.shiro.server.shiro.filter;

import com.alibaba.fastjson.JSONObject;
import com.yb.common.server.dic.UserConstant;
import com.yb.common.server.other.LoginUser;
import com.yb.common.server.utils.JwtTool;
import com.yb.common.server.utils.LoginUserUtils;
import com.yb.shiro.server.shiro.response.JwtToken;
import jdk.nashorn.internal.runtime.options.Option;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;
import sun.nio.ch.IOUtil;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * 注意:
 * 这个过滤器是基于网关认证的前提下的,没有对请求做严格的登录认证
 * 这个是在shiroConfig里new使用的.这里就不加注解实例化了
 * <p>
 * 其实rememberMe没有必要
 * 关于shiro的rememberMe的问题, 因为使用jwt, 所以根本不需要这个东西, 而且前端可以控制,
 * 而且通过refreshToken就可以实现, refreshToken的过期时间设置一般都比较长, 自己可设置,
 * 通过refreshToken请求token即可, 也就是通过刷新token的认证获取token
 *
 * @author biaoyang
 */
@Slf4j
public class JwtFilter extends BasicHttpAuthenticationFilter {

    /**
     * 判断用户是否想要登入
     * 检测header里面是否包含Authorization字段,并认证
     */
    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        String authorization = super.getAuthzHeader(request);
        //认证token的合法性,如果返回空则表示认证失败(设定荷载一定有用户信息)
        return Objects.nonNull(JwtTool.checkAndGetLoginUser(authorization));
    }

    /**
     * 执行登录操作,就是在这里进行token的合法性验证
     *
     * @param request
     * @param response
     * @return
     */
    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) {
        String authorization = super.getAuthzHeader(request);
        //构造JwtToken对象,这个实现了AuthenticationToken,
        //通过这个自定义的类把token信息通过shiro的登录方法,
        //传递到AuthorizingRealm的认证和授权方法里进行处理,
        JwtToken token = new JwtToken(authorization);
        //解析token信息,这里面一定要有用户的权限信息(角色信息)
        LoginUser loginUser = JwtTool.checkAndGetLoginUser(authorization);
        if (Objects.isNull(loginUser)) {
            //这里直接抛出异常,是为了不去后面再去验证了,同时也是保证shiro调用登录的时候一定可以认证通过
            throw new UnauthorizedException("认证失败");
        }
        //=========这里既先认证了token,也校验了jti,再调用shiro去登录一般不会抛出异常了,因为里面也只是再调用JwtTool解析而已,
        //=========而这里已经认证过了,调用shiro只是走过场而已(让shiro认证登录成功,便于接口的权限认证)
        //shiro没有security那样的直接构造上下文的方法,需要这样实现登录
        //注意,这里的AuthorizingRealm认证和授权方法,没有处理去数据查询的那种业务,而是处理
        //提交给realm进行登入(在里面授权),如果错误他会抛出异常并被捕获
        SecurityUtils.getSubject().login(token);
        //设置额外的信息
        HttpServletRequest req = (HttpServletRequest) request;
        loginUser.setIp(this.getIpAddress(req));
        loginUser.setUri(req.getRequestURI());
        loginUser.setFrom(req.getHeader("From"));
        LoginUserUtils.setUser(loginUser);
        // 如果没有抛出异常则代表登入成功，返回true
        return true;
    }

    /**
     * ----------注意---这个无脑返回true是在请求经过网关认证后的到这的前提下的,如果没有经过网关的认证,那么这里是不能这么写的----------
     * ----------------如果没有经过网关的认证,那么就需要在这里做认证,通过String path = request.getPath().value();可以判断是否是对应的登录
     * 注册url,然后放过拦截即可,这个方式弄起来特别麻烦而且容易出错,其实可以直接把那些不需要登录的接口方法统一放在一个不加 @RequiresAuthentication来
     * 指明需要登录即可,其他的需要登录的controller类都加上@RequiresAuthentication就可以了
     * ------shiroConfig里虽然可以配置放过登录,但是需要在过滤器之前,也就是过滤器的设置需要在后面----------------
     * <p>
     * 这里我们详细说明下为什么最终返回的都是true，即允许访问
     * 例如我们提供一个地址 GET /article
     * 登入用户和游客看到的内 容是不同的
     * 如果在这里返回了false，请求会被直接拦截，用户看不到任何东西
     * 所以我们在这里返回true，Controller中可以通过 subject.isAuthenticated() 来判断用户是否登入
     * 如果有些资源只有登入用户才能访问,们只需要在方法上面加上 @RequiresAuthentication 注解即可
     * 但是这样做有一个缺点,就是不能够对GET,POST等请求进行分别过滤鉴权(因为我们重写了官方的方法),但实际上对应用影响不大
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        //这里统一处理过滤器抛出的异常
        try {
            //当请求带有token,则表示这个请求需要通过shiro去登录,因为如果请求有权限限制,
            //需要通过shiro去登录拿到它的权限等信息,这样才能通过加了权限的接口的认证
            if (isLoginAttempt(request, response)) {
                //主要就是让shiro能够认证接口权限(把用户的权限放进shiro,然后请求的时候认证其是否有权限访问接口)
                executeLogin(request, response);
            }
        } catch (Exception e) {
            //自定义异常的类,用户返回给客户端相应的JSON格式的信息
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", HttpStatus.BAD_REQUEST.value());
            jsonObject.put("message", Objects.nonNull(e.getCause()) ? e.getCause().getMessage() : e.getMessage());
            response.setContentType("application/json; charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            String resultResponse = JSONObject.toJSONString(jsonObject);
            OutputStream out = null;
            try {
                out = WebUtils.toHttp(response).getOutputStream();
                out.write(resultResponse.getBytes("UTF-8"));
            } catch (IOException ex) {
                IOUtils.closeQuietly(out);
                log.error("response获取输出流错误:", ex);
            }
            log.error("executeLogin执行错误:", e);
            return false;
        }
        //还有就是如果真的有不需要登录的需求 ,那么需要在这里对那些不需要登录就能访问的url进行特殊处理,像网关那样匹配放过,当然了还有一个
        // 解决方案,就是把那些不需要登录的接口(url),统一放到一个controller类里,这里不加@RequiresAuthentication,就可以了,比比对请求url方便太多了
        //我这里默认设置所有请求必须登录,因为我在接口类上添加了@RequiresAuthentication保证请求必须要登录才能通过
        //===============================================================================================================
        //-------------------为了保证安全性,在接口类上添加@RequiresAuthentication注解,来保证登录才能访问,具体再认证权限
        //这里全都返回true,是因为网关已经对需要登录的请求做了认证,只有认证通过了才会到某个工程里过滤器(如,现在的这个过滤器)
        //所以进来的都是已经通过登录认证的了,这里所做的就是把具有token信息的请求里的token通过shiro把其权限信息给shiro,然后他
        //去请求接口的时候,如果接口有认证权限注解,或者接口的代码里有需要通过登录用户的信息才处理业务,就可以通过shiro登录时放进
        //LoginUserUtils,并通过此方法获取用户登录信息,而那些没有带有token的请求,既然能到这里来,说明了网关那里已经放过它了,也就是它
        //不用登录就可以访问(有权限的接口它也访问不了,可能接口都找不到地方点击,因为没有带有权限信息,所以被前端给隐藏了)
        return true;
    }

    /**
     * 获取当前访问的ip地址
     *
     * @param request
     * @return
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = null;
        String unknown = "unknown";
        String[] headers = {"x-forwarded-for", "Proxy-Client-IP", "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
        for (String header : headers) {
            ip = request.getHeader(header);
            if (StringUtils.isNotBlank(ip) && !unknown.equalsIgnoreCase(ip)) {
                break;
            }
        }
        if (StringUtils.isBlank(ip) || unknown.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }
}
