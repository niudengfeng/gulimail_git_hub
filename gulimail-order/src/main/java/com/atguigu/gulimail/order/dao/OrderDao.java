package com.atguigu.gulimail.order.dao;

import com.atguigu.gulimail.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author niudengfeng
 * @email 519507446@qq.com
 * @date 2021-03-23 15:00:44
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
