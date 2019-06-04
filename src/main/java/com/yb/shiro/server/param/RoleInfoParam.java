package com.yb.shiro.server.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * 这里的角色名是用来展示的,时间的意义也差不多
 * @author biaoyang
 */
@Setter
@Getter
@ApiModel(description = "角色基本信息类")
public class RoleInfoParam{

    @NotBlank(message = "关联的用户id不能为空")
    @Length(max = 50,message = "关联的用户id有误")
    @ApiModelProperty("关联的用户id")
    private String userId;

    @NotBlank(message = "角色不能为空")
    @Length(max = 15,message = "角色有误")
    @ApiModelProperty("角色")
    private String role;

    @NotBlank(message = "角色名称不能为空")
    @Length(max = 25,message = "角色名称有误")
    @ApiModelProperty("角色名称")
    private String roleName;

}
