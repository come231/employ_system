package com.hkd.mapper;

import com.hkd.pojo.SysOperationLog;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface SysOperationLogDao {
    // 查询所有操作日志数量
    Integer findAllCount();

    // 查询所有操作日志(分页)
    List<SysOperationLog> findAll(@Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    // 根据ID查询操作日志
    SysOperationLog findById(Long logId);

    // 根据用户名查询数量
    Integer findCountByUsername(String username);

    // 根据用户名查询操作日志(分页)
    List<SysOperationLog> findByUsername(@Param("username") String username, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    // 根据模块查询数量
    Integer findCountByModule(String module);

    // 根据模块查询操作日志(分页)
    List<SysOperationLog> findByModule(@Param("module") String module, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    // 根据操作类型查询数量
    Integer findCountByOperationType(String operationType);

    // 根据操作类型查询操作日志(分页)
    List<SysOperationLog> findByOperationType(@Param("operationType") String operationType, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    // 根据日期范围查询数量
    Integer findCountByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    // 根据日期范围查询操作日志(分页)
    List<SysOperationLog> findByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    // 新增操作日志
    int insert(SysOperationLog sysOperationLog);
}