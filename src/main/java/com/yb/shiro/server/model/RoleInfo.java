package com.yb.shiro.server.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 这里的角色名是用来展示的,时间的意义也差不多
 *
 * @author biaoyang
 */
@Setter
@Getter
@Document(collection = "RoleInfo")
@ApiModel(description = "角色基本信息类")
public class RoleInfo implements Serializable {
    private static final long serialVersionUID = -6039356831089267546L;

    @ApiModelProperty("角色id")
    private String id;

    @ApiModelProperty("角色")
    private String role;

    @ApiModelProperty("角色名称")
    private String roleName;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    public RoleInfo() {
        this.id = UUID.randomUUID().toString().replace("-", "");
        this.createTime = LocalDateTime.now();
    }

}
