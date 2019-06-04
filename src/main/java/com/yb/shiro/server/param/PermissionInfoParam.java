package com.yb.shiro.server.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotBlank;

/**
 * 权限这里和角色是多个对一的关系
 *
 * @author biaoyang
 */
@Setter
@Getter
@ApiModel(description = "权限参数接收类")
public class PermissionInfoParam{

    @Length(max = 50,message = "角色id有误")
    @ApiModelProperty("角色id")
    private String roleId;

    @NotBlank(message = "权限不能为空")
    @Length(max = 25,message = "权限有误")
    @ApiModelProperty("权限")
    private String permission;

    @NotBlank(message = "权限所属系统不能为空")
    @Length(max = 25,message = "权限所属系统有误")
    @ApiModelProperty("权限所属系统")
    private String permissionSystem;

}
