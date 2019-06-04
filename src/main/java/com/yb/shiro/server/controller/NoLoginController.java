package com.yb.shiro.server.controller;

import com.yb.common.server.other.Result;
import com.yb.shiro.server.param.UserLoginParam;
import com.yb.shiro.server.param.UserRegisterParam;
import com.yb.shiro.server.service.ShiroUserService;
import com.yb.shiro.server.shiro.response.JwtToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

/**
 * 其实这里的接口也不用通过ShiroConfig配置放过,
 * 因为这里没有给类加@RequiresAuthentication保证登录
 *
 * @author biaoyang
 */
@Api(tags = "不用登录的接口类")
@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/noLogin")
public class NoLoginController {
    private final ShiroUserService shiroUserService;

    @ApiOperation("用户登录获取token")
    @PostMapping("/login")
    public Result<JwtToken> login(@Valid @RequestBody UserLoginParam userLoginParam) {
        JwtToken result = shiroUserService.login(userLoginParam);
        return Result.success(result);
    }

    @ApiOperation("注册用户")
    @PostMapping("/registerUser")
    public Result<String> registerUser(@Valid @RequestBody UserRegisterParam userRegisterParam) {
        String result = shiroUserService.registerUser(userRegisterParam);
        return Result.success(result);
    }

}
