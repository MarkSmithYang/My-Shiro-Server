package com.yb.shiro.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.UUID;

/**
 * 这个是用户的基础信息,方便用来登录,详情信息需要在创建一个类(表),用同样的主键id关联
 * 这样通过用户id就可以去直接查询用户详情信息,而不用去联合查询了,可以详情信息随意拓展无数个表
 * @author biaoyang
 */
@Setter
@Getter
@Document(collection = "UserInfo")
@ApiModel(description = "用户基本信息类")
public class UserInfo implements Serializable {
    private static final long serialVersionUID = 8184196145168115245L;

    @Id
    @ApiModelProperty("主键id")
    private String id;

    @ApiModelProperty("用户名")
    private String username;

    @JsonIgnore
    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("用户状态:启用中,禁用中")
    private String userStatus;

    public UserInfo() {
        this.id= UUID.randomUUID().toString().replace("-","");
        this.userStatus="启用中";
    }

}
