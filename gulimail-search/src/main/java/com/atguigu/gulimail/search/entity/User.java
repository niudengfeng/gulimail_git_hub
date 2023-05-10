package com.atguigu.gulimail.search.entity;

import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@ApiModel(value = "用户ES对象")
@ToString
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "姓名")
    private String userName;
    @ApiModelProperty(value = "性别")
    private String gender;
    @ApiModelProperty(value = "年龄")
    private Integer age;

}
