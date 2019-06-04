package com.yb.shiro.server.repository;

import com.yb.shiro.server.model.UserInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author biaoyang
 */
public interface UserInfoRepository extends MongoRepository<UserInfo,String> {

    /**
     * 通过用户名查询用户信息
     * @param trim
     * @return
     */
    UserInfo findByUsername(String trim);
}
