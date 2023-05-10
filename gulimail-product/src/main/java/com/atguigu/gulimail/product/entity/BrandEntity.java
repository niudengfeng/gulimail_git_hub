package com.atguigu.gulimail.product.entity;

import com.atguigu.common.valid.service.AddGroup;
import com.atguigu.common.valid.service.ListValue;
import com.atguigu.common.valid.service.updateGroup;
import com.atguigu.common.valid.service.updateStatusGroup;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 * 
 * @author niudengfeng
 * @email 519507446@qq.com
 * @date 2021-03-23 14:57:19
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@NotNull(message = "修改必须指定id" ,groups = {updateGroup.class, updateStatusGroup.class})
	@Null(message = "新增不可指定id",groups = {AddGroup.class})
	@TableId
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "品牌名不能为空",groups = {updateGroup.class,AddGroup.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@NotBlank(message = "logo不能为空",groups = {updateGroup.class,AddGroup.class})
	@URL(message = "logo必须是合法URL地址",groups = {updateGroup.class,AddGroup.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@NotNull(message = "显示状态不能为空",groups = {updateGroup.class,AddGroup.class})
	@ListValue(vals={0,1},message="显示状态字段只允许为0和1",groups = {updateGroup.class,AddGroup.class})
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@NotNull(message = "首字母不能为空",groups = {updateGroup.class,AddGroup.class})
	@Pattern(regexp = "^[a-zA-Z]$",message = "首字母必须是一位英文大小写字母",groups = {updateGroup.class,AddGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@NotNull(message = "排序不能为空",groups = {updateGroup.class,AddGroup.class})
	@Min(value = 0,message = "排序字段必须大于等于0",groups = {updateGroup.class,AddGroup.class})
	private Integer sort;

	/**
	 * 状态（1启用，0停用）
	 */
//	@NotNull(message = "状态不能为空",groups = {updateGroup.class,updateStatusGroup.class,AddGroup.class})
//	@ListValue(vals={0,1},message="状态字段只允许为0和1",groups = {updateGroup.class,updateStatusGroup.class,AddGroup.class})
//	private Integer status=0;

}
