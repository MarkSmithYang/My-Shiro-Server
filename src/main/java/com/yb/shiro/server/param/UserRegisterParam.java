package com.yb.shiro.server.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @author biaoyang
 */
@Setter
@Getter
@ApiModel(description = "用户注册参数封装类")
public class UserRegisterParam {

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "[0-9a-zA-Z]{4,16}", message = "用户名只能是字母和数字的组合且长度为4到16字")
    @ApiModelProperty("用户名")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "[0-9a-zA-Z]{4,18}", message = "密码只能是字母和数字的组合且长度为4到18字")
    @ApiModelProperty("密码")
    private String password;

}
