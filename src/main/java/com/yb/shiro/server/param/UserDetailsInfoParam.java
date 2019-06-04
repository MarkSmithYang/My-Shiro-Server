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
@ApiModel(description = "用户详情参数封装类")
public class UserDetailsInfoParam {

    @NotBlank(message = "姓名不能为空")
    @Length(max = 25, message = "姓名过长")
    @ApiModelProperty("姓名")
    private String fullName;

    @Pattern(regexp = "^(((\\+\\d{2}-)?0\\d{2,3}-\\d{7,8})|((\\+\\d{2}-)?(\\d{2,3}-)?([1][3,4,5,7,8][0-9]\\d{8})))$",message = "手机号码有误")
    @ApiModelProperty("手机号")
    private String cellphone;

    @Length(max = 25, message = "机构代码有误")
    @ApiModelProperty("机构代码")
    private String orgCode;

    @Length(max = 50, message = "机构名称(部门)有误")
    @ApiModelProperty("机构名称(部门)")
    private String orgName;

}
