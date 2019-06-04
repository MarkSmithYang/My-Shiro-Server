package com.yb.shiro.server.repository;

import com.yb.shiro.server.model.UserRoleInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author biaoyang
 */
public interface UserRoleInfoRepository extends MongoRepository<UserRoleInfo,String> {

    /**
     * 通过用户id查询关联角色中间表的信息
     * @param id
     * @return
     */
    List<UserRoleInfo> findByUserId(String id);

}
