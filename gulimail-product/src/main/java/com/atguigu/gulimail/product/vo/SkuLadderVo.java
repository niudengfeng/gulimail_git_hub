package com.atguigu.gulimail.product.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品阶梯价格
 * 
 * @author niudengfeng
 * @email 519507446@qq.com
 * @date 2021-03-23 15:04:26
 */
@Data
@ToString
public class SkuLadderVo{

	/**
	 * id
	 */
	private Long id;
	/**
	 * spu_id
	 */
	private Long skuId;
	/**
	 * 满几件
	 */
	private Integer fullCount;
	/**
	 * 打几折
	 */
	private BigDecimal discount;
	/**
	 * 折后价
	 */
	private BigDecimal price;
	/**
	 * 是否叠加其他优惠[0-不可叠加，1-可叠加]
	 */
	private Integer addOther;

}
