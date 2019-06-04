package com.yb.shiro.server.repository;

import com.yb.shiro.server.model.RoleInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author biaoyang
 */
public interface RoleInfoRepository extends MongoRepository<RoleInfo,String> {

    /**
     * 通过角色id查询角色信息
     * @param roleIds
     * @return
     */
    List<RoleInfo> findByIdIn(List<String> roleIds);
}
