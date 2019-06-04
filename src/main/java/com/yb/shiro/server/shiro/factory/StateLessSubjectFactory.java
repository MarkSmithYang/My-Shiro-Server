package com.yb.shiro.server.shiro.factory;

import com.yb.shiro.server.shiro.response.JwtToken;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.web.mgt.DefaultWebSubjectFactory;

/**
 * 扩展自DefaultWebSubjectFactory,对于无状态的TOKEN 类型不创建session
 * 使用session会有分布式无法共享的问题,还有session的时间过期等问题,需要处理
 * 本身jwt这样的无状态token,已经很好的实现了单点登录和分布式的问题,因为每次都是通过
 * 过滤器来进行jwt的token验证,每个请求带有token信息,而不是使用session那样去session里获取
 * 那个session需要通过shiro-redis依赖等整合redis存储session信息,才能达到分布式的session共享
 * 也就是用户的登录信息不会因为请求的时候负载均衡到别的服务器上的服务而导致登录失效,因为
 * 登录的session信息不是存储在单个服务器上的,而是存储在redis上的,解决了分布式集群部署的时候
 * session无法共享的问题,这个和spring的session整合redis的session共享其实是一个道理,就是session
 * 会话统一放在一处,在使用的时候直接去redis获取即可,而不是通过本身的服务器存储(无法解决不同服务器间的session共享)
 * jwt就可以很好的处理上面的session信息共享问题,现在就应该使用jwt的方式,而尽量不去使用session的方式,即便是前后端不分离
 * 的情况也是这样的处理,可以把jwt放在redis上,这个就是用来替代token放在请求头或者请求参数的做法,也就是登录信息在登录的
 * 时候存入到redis,在登录信息未过期的情况下,你打开网页都是可以访问的,因为请求进来的时候经过过滤器,过滤器回去redis里查询
 * 的登录信息是否存在,如果存在则让你请求服务,通过redis存储的token方式代替token放在请求的方式,道理其实一样,只是这个是会存在
 * 登录信息的过期问题的,和jwt一样可以通过清除redis上的信息来达到让登录信息失效,通过每次登录逇时候删除redis存储的登录信息来
 * 保证一个账号登录,也就是你在这登录,其他地方的登录就会失效而变成未登录状态了,不过一般不建议这么做,因为每个基本有登录手机和电脑的需求
 * 如果真的有这样的需求,每次登录的时候清除以前的登录信息即可,一般都是通过唯一的用户名作为redis存储token的key,这个就方便处理同一个账号登录的问题
 *
 * @author biaoyang
 */
public class StateLessSubjectFactory extends DefaultWebSubjectFactory {

    @Override
    public Subject createSubject(SubjectContext context) {
        //这里其实就是在创建subject之前做了增强(就是关闭session)
        AuthenticationToken token = context.getAuthenticationToken();
        if ((token instanceof JwtToken)) {
            // 当token为jwtToken时,不创建 session
            context.setSessionCreationEnabled(false);
        }
        //关闭token后,调用父类原来的方法创建subject
        return super.createSubject(context);
    }
}

