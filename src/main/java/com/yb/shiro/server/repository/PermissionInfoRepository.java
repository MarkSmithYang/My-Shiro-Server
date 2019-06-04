package com.yb.shiro.server.repository;

import com.yb.shiro.server.model.PermissionInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/**
 * @author biaoyang
 */
public interface PermissionInfoRepository extends MongoRepository<PermissionInfo,String> {

    /**
     * 通过角色id获取权限信息
     * @param roleIds
     * @return
     */
    @Query()
    List<PermissionInfo> findByRoleIdIn(List<String> roleIds);

}
