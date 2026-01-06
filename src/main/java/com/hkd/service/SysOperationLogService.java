package com.hkd.service;

import com.hkd.pojo.SysOperationLog;

import java.util.Date;
import java.util.List;

public interface SysOperationLogService {
    // 查询所有操作日志数量
    Integer findAllCount();

    // 查询所有操作日志(分页)
    List<SysOperationLog> findAll(Integer offset, Integer pageSize);

    // 根据ID查询操作日志
    SysOperationLog findById(Long logId);

    // 根据用户名查询数量
    Integer findCountByUsername(String username);

    // 根据用户名查询操作日志(分页)
    List<SysOperationLog> findByUsername(String username, Integer offset, Integer pageSize);

    // 根据模块查询数量
    Integer findCountByModule(String module);

    // 根据模块查询操作日志(分页)
    List<SysOperationLog> findByModule(String module, Integer offset, Integer pageSize);

    // 根据操作类型查询数量
    Integer findCountByOperationType(String operationType);

    // 根据操作类型查询操作日志(分页)
    List<SysOperationLog> findByOperationType(String operationType, Integer offset, Integer pageSize);

    // 根据日期范围查询数量
    Integer findCountByDateRange(Date startDate, Date endDate);

    // 根据日期范围查询操作日志(分页)
    List<SysOperationLog> findByDateRange(Date startDate, Date endDate, Integer offset, Integer pageSize);

    // 新增操作日志
    int insert(SysOperationLog sysOperationLog);
}
