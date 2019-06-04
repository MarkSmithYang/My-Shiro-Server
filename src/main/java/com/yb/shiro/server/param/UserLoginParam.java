package com.yb.shiro.server.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @author biaoyang
 */
@Setter
@Getter
@ApiModel(description = "用户登录参数封装类")
public class UserLoginParam {

    @NotBlank(message = "用户名不能为空")
    @Length(min = 4, max = 16, message = "用户名或密码有误")
    @ApiModelProperty("用户名")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Length(min = 4,max = 18, message = "用户名或密码有误")
    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("记住我")
    private Boolean remberMe;

}
