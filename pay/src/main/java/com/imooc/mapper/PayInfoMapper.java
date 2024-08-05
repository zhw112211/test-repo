package com.imooc.mapper;

import com.imooc.pojo.PayInfo;

public interface PayInfoMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(PayInfo record);

    int insertSelective(PayInfo record);

    PayInfo selectByPrimaryKey(Integer id);

    /**
     * 根据订单号查询订单状态
     * @param orderNo
     * @return
     */
    PayInfo selectByOrderNo(Long orderNo);

    int updateByPrimaryKeySelective(PayInfo record);

    int updateByPrimaryKey(PayInfo record);
}