package com.yb.shiro.server.repository;

import com.yb.shiro.server.model.UserInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author biaoyang
 */
public interface ModuleInfoRepository extends MongoRepository<UserInfo,String> {

}
