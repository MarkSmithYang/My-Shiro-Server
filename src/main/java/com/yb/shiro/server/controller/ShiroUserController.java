package com.yb.shiro.server.controller;

import com.yb.common.server.other.Result;
import com.yb.shiro.server.model.PermissionInfoParam;
import com.yb.shiro.server.model.RoleInfo;
import com.yb.shiro.server.model.RoleInfoParam;
import com.yb.shiro.server.model.UserInfo;
import com.yb.shiro.server.param.UserDetailsInfoParam;
import com.yb.shiro.server.param.UserLoginParam;
import com.yb.shiro.server.param.UserRegisterParam;
import com.yb.shiro.server.service.ShiroUserService;
import com.yb.shiro.server.shiro.response.JwtToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 这里业务只做了注册和登录,修改密码等其他的功能都省略了
 * 也没有单独创建一个专门用来处理用户相关的工程,直接是放
 * 在shiro-server里弄的,事实上,这个就是一个User操作的项目
 *这里的@RequiresAuthentication,就可以全部拦截那些没有登录认证的请求------这个在没有网关的时候可以很好的保护安全,
 *其中注册和登录已经在ShiroConfig里配置了放过,所以这里不会拦截
 * 这个不能解决---------------------网关放过来的不用登录的接口的问题,具体情况具体处理
 * @author biaoyang
 */
@Api(tags = "用户操作有关的控制类")
@Validated
@RestController
@AllArgsConstructor
@RequiresAuthentication
@RequestMapping("/user")
public class ShiroUserController {

    private final ShiroUserService shiroUserService;

    @ApiOperation("添加用户详情信息")
    @PostMapping("/addUserDetails")
    public Result<String> addUserDetails(@Valid @RequestBody UserDetailsInfoParam userDetailsInfoParam) {
        String result = shiroUserService.addUserDetails(userDetailsInfoParam);
        return Result.success(result);
    }

    @RequiresPermissions("sys:user:permission:create")
    @ApiOperation("创建权限")
    @PostMapping("/createPermission")
    public Result<String> createPermission(@Valid @RequestBody PermissionInfoParam permissionInfoParam) {
        String result = shiroUserService.createPermission(permissionInfoParam);
        return Result.success(result);
    }

    @RequiresPermissions("sys:user:role:create")
    @ApiOperation("创建角色")
    @PostMapping("/createRole")
    public Result<String> createRole(@Valid @RequestBody RoleInfoParam roleInfoParam) {
        String result = shiroUserService.createRole(roleInfoParam);
        return Result.success(result);
    }

    @RequiresPermissions("sys:user:admin:view:role")
    @ApiOperation("获取角色列表")
    @GetMapping("/findRoleList")
    public Result<List<RoleInfo>> findRoleList() {
        List<RoleInfo> result = shiroUserService.findRoleList();
        return Result.success(result);
    }

    @RequiresPermissions("sys:user:admin:view:user")
    @ApiOperation("获取用户列表")
    @GetMapping("/findUserList")
    public Result<List<UserInfo>> findUserList() {
        List<UserInfo> result = shiroUserService.findUserList();
        return Result.success(result);
    }

    @RequiresPermissions("sys:user:admin:view:details")
    @ApiOperation("根据用户id获取用户信息")
    @GetMapping("/findUserById")
    public Result<UserInfo> findUserById(
            @ApiParam(value = "用户id", required = true)
            @NotBlank(message = "用户id不能为空")
            @Length(max = 50, message = "用户id有误")
            @RequestParam String id) {
        UserInfo result = shiroUserService.findUserById(id);
        return Result.success(result);
    }

}
