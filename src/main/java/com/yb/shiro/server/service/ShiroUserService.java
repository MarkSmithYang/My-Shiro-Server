package com.yb.shiro.server.service;

import com.yb.common.server.dic.JwtDic;
import com.yb.common.server.dic.UserConstant;
import com.yb.common.server.other.LoginUser;
import com.yb.common.server.utils.JwtTool;
import com.yb.common.server.utils.LoginUserUtils;
import com.yb.shiro.server.model.*;
import com.yb.shiro.server.model.PermissionInfoParam;
import com.yb.shiro.server.model.RoleInfoParam;
import com.yb.shiro.server.param.UserDetailsInfoParam;
import com.yb.shiro.server.param.UserLoginParam;
import com.yb.shiro.server.param.UserRegisterParam;
import com.yb.shiro.server.repository.*;
import com.yb.shiro.server.shiro.response.JwtToken;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author biaoyang
 */
@Service
@AllArgsConstructor
public class ShiroUserService {
    private final RoleInfoRepository roleInfoRepository;
    private final UserInfoRepository userInfoRepository;
    private final UserRoleInfoRepository userRoleInfoRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RedisTemplate<String, Serializable> redisTemplate;
    private final PermissionInfoRepository permissionInfoRepository;
    private final UserDetailsInfoRepository userDetailsInfoRepository;

    /**
     * 用户登录获取token
     * 这里是脱离shiro的,这里直接做验证返回token,走shiro的时候,就是过滤器的那个认证token有效性的时候
     * 也就是说当需要shiro登录认证的时候再去调用shiro的东西,而不是登录这里就去调用shiro,这样的话,就可以
     * 减少很多重复的逻辑,也不用再去写两个shiro的认证逻辑(token认证和用户名密码认证),而且这里调用shiro
     * 登录的意义也不大,反正你请求其他接口的时候是要经过过滤器的,那时候认证shiro才是更好的做法,这个毕竟
     * 不是单个项目的,而且在登录这里就进行shiro的认证,有点多余和冗余,因为其他的服务接口还是得经过过滤器
     * 的合法性认证,何苦在这调用shiro认证了,不仅没什么用,还要增加些代码的难度,做兼容,因为只有一个AuthorizingRealm
     * 的时候,你的那个认证和授权都是在那里面做的,你不仅要处理token的认证,还需要处理用户名和密码的逻辑,所以这里不用shiro
     *
     * @param userLoginParam
     * @return
     */
    public JwtToken login(UserLoginParam userLoginParam) {
        //通过用户名查询数据库用户信息
        UserInfo userInfo = userInfoRepository.findByUsername(userLoginParam.getUsername().trim());
        if (Objects.isNull(userInfo)) {
            throw new UnauthorizedException("用户名或密码有误");
        }
        //校验用户的密码是否正确
        if (!bCryptPasswordEncoder.matches(userLoginParam.getPassword().trim(), userInfo.getPassword())) {
            throw new UnauthorizedException("用户名或密码有误");
        }
        //校验用户的状态是否是启用中的
        if (!UserConstant.USER_STATUS.equals(userInfo.getUserStatus())) {
            throw new UnauthorizedException("用户名已经被禁用");
        }
        //验证成功,生成token信息(默认秘钥的方式),这里因为没有设置用户的详情信息,
        UserDetailsInfo userDetailsInfo = userDetailsInfoRepository.findById(userInfo.getId()).orElseGet(UserDetailsInfo::new);
        //而LoginUser放的都是不敏感的信息,都是通过这里类来封装用户的详情信息(部门,区域等),主要有权限和角色信息等信息
        LoginUser loginUser = new LoginUser();
        //由于这里用的mongo,而不是mysql,可以用mybatis(plus)直接操作表,而不用些实体
        loginUser.setUserId(userInfo.getId());
        loginUser.setUsername(userInfo.getUsername());
        //获取用户的角色权限信息,由于使用的mongodb,所以联合查询只会$lookup的两张表查询,三张表的不会,拆开写
        List<UserRoleInfo> userRoles = userRoleInfoRepository.findByUserId(userInfo.getId());
        if (CollectionUtils.isNotEmpty(userRoles)) {
            //获取角色集合
            List<String> roleIds = userRoles.stream().map(s -> s.getRoleId()).collect(Collectors.toList());
            //查询角色关联的权限信息,权限全部只与角色关联,不与用户直接关联,而需要通过角色关联
            List<PermissionInfo> permissionInfos = permissionInfoRepository.findByRoleIdIn(roleIds);
            //通过角色ID查询角色信息
            List<RoleInfo> roleInfos = roleInfoRepository.findByIdIn(roleIds);
            //判断权限集并处理数据
            if (CollectionUtils.isNotEmpty(permissionInfos)) {
                Set<String> permissions = permissionInfos.stream()
                        .map(g -> g.getPermission())
                        .collect(Collectors.toSet());
                loginUser.setPerms(permissions);
            }
            //判断角色集并处理数据
            if (CollectionUtils.isNotEmpty(roleInfos)) {
                Set<String> roles = roleInfos.stream()
                        .map(g -> g.getRole())
                        .collect(Collectors.toSet());
                loginUser.setRoles(roles);
            }
        }
        //保存jti为30分钟,过了30分则登录信息失效,这个时间最好和设置的token过期时间一致
        String jti = JwtTool.createJti();
        redisTemplate.opsForValue().set(UserConstant.USER_SYSTEM + userInfo.getUsername(), jti,
                JwtDic.ACCESS_EXPIRATION_SECONDS, TimeUnit.SECONDS);
        //保存jti到LoginUser
        loginUser.setJti(jti);
        //因为没做详情的保存(也就是没用代码去关联而是直接在数据表关联的,所以为了保证用户名一致性,就用set了,所以就排除了)
        BeanUtils.copyProperties(userDetailsInfo, loginUser, "username", "id");
        //过期时间是毫秒值,需要转换一下
        String accessToken = JwtTool.createAccessToken(loginUser, JwtDic.ACCESS_EXPIRATION_SECONDS * 1000);
        String refreshToken = JwtTool.createAccessToken(loginUser, JwtDic.REFRESH_EXPIRATION_SECONDS * 1000);
        //返回token信息
        return new JwtToken(accessToken, refreshToken, UserConstant.HEADER_TYPE, JwtDic.ACCESS_EXPIRATION_SECONDS);
    }

    /**
     * 注册用户
     *
     * @param userRegisterParam
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public String registerUser(UserRegisterParam userRegisterParam) {
        //通过用户名校验用户名是否已经存在
        UserInfo userInfo = userInfoRepository.findByUsername(userRegisterParam.getUsername().trim());
        if (Objects.nonNull(userInfo)) {
            throw new IllegalArgumentException("用户名已经被注册");
        }
        //保存用户信息
        UserInfo user = new UserInfo();
        user.setUsername(userRegisterParam.getUsername());
        //加密密码
        user.setPassword(bCryptPasswordEncoder.encode(userRegisterParam.getPassword()));
        //保存用户信息
        userInfoRepository.save(user);
        //返回操作
        return "操作成功";
    }

    /**
     * 获取用户列表
     *
     * @return
     */
    public List<UserInfo> findUserList() {
        return userInfoRepository.findAll();
    }

    /**
     * @return 根据用户id获取用户信息
     */
    public UserInfo findUserById(String id) {
        return userInfoRepository.findById(id).orElse(null);
    }

    /**
     * 创建权限
     *
     * @param permissionInfoParam
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public String createPermission(PermissionInfoParam permissionInfoParam) {
        //封装数据
        PermissionInfo permissionInfo = new PermissionInfo();
        BeanUtils.copyProperties(permissionInfoParam, permissionInfo);
        //保存权限
        permissionInfoRepository.save(permissionInfo);
        //返回操作
        return "操作成功";
    }

    /**
     * 创建角色
     *
     * @param roleInfoParam
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public String createRole(RoleInfoParam roleInfoParam) {
        //查询关联的用户的id的用户是否存在
        UserInfo userInfo = userInfoRepository.findById(roleInfoParam.getUserId()).orElse(null);
        if (Objects.isNull(userInfo)) {
            throw new IllegalArgumentException("关联的用户id有误");
        }
        //封装角色数据
        RoleInfo roleInfo = new RoleInfo();
        BeanUtils.copyProperties(roleInfoParam, roleInfo);
        //封装关联用户和角色的中间表的数据,中间表的id用mongo给的,这里不指定了
        UserRoleInfo userRoleInfo = new UserRoleInfo();
        userRoleInfo.setRoleId(roleInfo.getId());
        userRoleInfo.setUserId(userInfo.getId());
        //保存角色和用户角色关联信息
        roleInfoRepository.save(roleInfo);
        userRoleInfoRepository.save(userRoleInfo);
        //返回操作
        return "操作成功";
    }

    /**
     * 添加用户详情信息
     *
     * @param userDetailsInfoParam
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public String addUserDetails(UserDetailsInfoParam userDetailsInfoParam) {
        //获取登录的用户名
        String username = LoginUserUtils.getUsername().orElseThrow(() -> new UnauthorizedException("登录异常"));
        String userId = LoginUserUtils.getUserId().orElseThrow(() -> new UnauthorizedException("登录异常"));
        //封装用户详情信息
        UserDetailsInfo userDetailsInfo = new UserDetailsInfo();
        userDetailsInfo.setId(userId);
        userDetailsInfo.setUsername(username);
        BeanUtils.copyProperties(userDetailsInfoParam, userDetailsInfo);
        //保存数据
        userDetailsInfoRepository.save(userDetailsInfo);
        //返回操作
        return "操作成功";
    }

    /**
     * 获取角色列表
     *
     * @return
     */
    public List<RoleInfo> findRoleList() {
        return roleInfoRepository.findAll();
    }
}
