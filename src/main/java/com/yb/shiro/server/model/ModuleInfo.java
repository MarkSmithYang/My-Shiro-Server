package com.yb.shiro.server.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * @author biaoyang
 */
@Setter
@Getter
@Document(collection = "ModuleInfo")
@ApiModel(description = "模块基本信息类")
public class ModuleInfo implements Serializable {
    private static final long serialVersionUID = -491240836452538280L;

    @ApiModelProperty("模块id")
    private String id;

    @ApiModelProperty("模块编码: 两位一级,便于区分模块")
    private Long moduleCode;

    @ApiModelProperty("模块")
    private String module;

    @ApiModelProperty("模块名称")
    private String moduleName;

    @ApiModelProperty("模块所属系统")
    private String moduleSystem;

}
