package com.yb.shiro.server.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.UUID;

/**
 * 权限这里和角色是多个对一的关系
 *
 * @author biaoyang
 */
@Setter
@Getter
@Document(collection = "PermissionInfo")
@ApiModel(description = "权限基本信息类")
public class PermissionInfo implements Serializable {
    private static final long serialVersionUID = 6221108464198216726L;

    @Id
    @ApiModelProperty("主键id")
    private String id;

    @ApiModelProperty("角色id")
    private String roleId;

    @ApiModelProperty("权限")
    private String permission;

    @ApiModelProperty("权限所属系统")
    private String permissionSystem;

    public PermissionInfo() {
        this.id = UUID.randomUUID().toString().replace("-", "");
    }
}
