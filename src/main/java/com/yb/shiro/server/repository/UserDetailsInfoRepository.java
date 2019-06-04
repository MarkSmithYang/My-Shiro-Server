package com.yb.shiro.server.repository;

import com.yb.shiro.server.model.UserDetailsInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author biaoyang
 */
public interface UserDetailsInfoRepository extends MongoRepository<UserDetailsInfo,String> {

}
