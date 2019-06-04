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
 * 存储一下用户名(比较常用),免得去联合查询,也便于封装用户名
 * @author biaoyang
 */
@Setter
@Getter
@Document(collection = "UserDetailsInfo")
@ApiModel(description = "用户详情信息类")
public class UserDetailsInfo implements Serializable {
    private static final long serialVersionUID = -8443932118981476501L;

    @Id
    @ApiModelProperty("主键id")
    private String id;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("姓名")
    private String fullName;

    @ApiModelProperty("手机号")
    private String cellphone;

    @ApiModelProperty("机构代码")
    private String orgCode;

    @ApiModelProperty("机构名称(部门)")
    private String orgName;

}
