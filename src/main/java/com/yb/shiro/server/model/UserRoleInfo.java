package com.yb.shiro.server.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * 这里用户和角色用一个中间表来关联,是因为这个是多对多的,而且这里没有使用jpa的@ManyToMany
 * 而且springboot的1.x版本才能自动创建中间表,建个实体维护可能好点,而且角色和权限做的是一对多
 * 而不是多对多,也就是角色可以只有一个权限,可以有多个,多个角色组合就可以组成完成的权限集,而且
 * 这样的设计,也是基于新RBAC的,也就是权限是基于资源的,而不是角色的,把资源包裹成一个或多个角色
 * 分配不同的用户,这样用户只跟角色来关联,这样就比以前的用户谁都关联的那种简洁而好用,而模块的设置
 * 是用来切分资源的,这个资源根据层级的拼接,就成了权限,认证的时候权限就是模块的资源,这可以随意的搭配,很灵活
 * 这里写实体,是因为使用的是mongo,这个用的很想jpa,所以建了实体,如果是mybatis(plus)的,可以很方便的处理表数据
 * 的情况,是可以不写的,还有就是如果是使用mysql的话,可以都使用long这样数字主键,mysql会自动自增的,这个对于查询
 * 操作用户会比UUID更快些
 * @author biaoyang
 */
@Setter
@Getter
@Document(collection = "UserRoleInfo")
@ApiModel(description = "用户角色关联基本信息类")
public class UserRoleInfo implements Serializable {
    private static final long serialVersionUID = 4026141010371608355L;

    @Id
    @ApiModelProperty("主键id")
    private String id;

    @ApiModelProperty("用户id")
    private String userId;

    @ApiModelProperty("角色id")
    private String roleId;

}
