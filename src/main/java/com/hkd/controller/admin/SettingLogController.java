package com.hkd.controller.admin;

import com.hkd.pojo.SysOperationLog;
import com.hkd.service.SysOperationLogService;
import com.hkd.util.CommonUtil;
import com.hkd.util.JsonUtil;
import com.hkd.util.PageQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 杨
 * @version 1.0
 * @date 2025/12/27
 * @description 操作日志Controller
 */
@Controller
@RequestMapping("/admin/setting/log")
public class SettingLogController {
    @Autowired
    private SysOperationLogService sysOperationLogService;

    @RequestMapping(value = "/data", method = RequestMethod.GET)
    @ResponseBody
    public String findAllOperationLog(HttpServletRequest request) {
        try {
            int offset = Integer.parseInt(request.getParameter("page"));
            int pageSize = Integer.parseInt(request.getParameter("pageSize"));
            PageQuery pageQuery = new PageQuery(offset, pageSize);
            offset = pageQuery.getOffset();
            pageSize = pageQuery.getPageSize();
            int totalCount = sysOperationLogService.findAllCount();
            List<Map<String, Object>> lists = new ArrayList<>();
            List<SysOperationLog> logs = sysOperationLogService.findAll(offset, pageSize);
            if (!CommonUtil.isEmpty(logs) && logs.size() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                for (SysOperationLog log : logs) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("logId", log.getLogId());
                    map.put("username", log.getUsername());
                    map.put("roleName", log.getRoleName());
                    map.put("module", log.getModule());
                    map.put("operationType", log.getOperationType());
                    map.put("operationContent", log.getOperationContent());
                    map.put("operationTime", log.getOperationTime() != null ? sdf.format(log.getOperationTime()) : "");
                    map.put("status", log.getStatus() != null ? log.getStatus().intValue() : 0);
                    map.put("errorMsg", log.getErrorMsg());
                    lists.add(map);
                }
            }
            return JsonUtil.buildJsonByTotalCount(lists, totalCount);
        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @ResponseBody
    public String findSearchOperationLog(HttpServletRequest request) {
        try {
            String type = request.getParameter("type");
            String data = request.getParameter("data");
            String startDateStr = request.getParameter("startDate");
            String endDateStr = request.getParameter("endDate");
            int offset = Integer.parseInt(request.getParameter("page"));
            int pageSize = Integer.parseInt(request.getParameter("pageSize"));
            PageQuery pageQuery = new PageQuery(offset, pageSize);
            offset = pageQuery.getOffset();
            pageSize = pageQuery.getPageSize();
            int totalCount = 0;
            List<SysOperationLog> logs = new ArrayList<>();

            // 日期范围搜索
            if (!CommonUtil.isEmpty(startDateStr) || !CommonUtil.isEmpty(endDateStr)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date startDate = null;
                Date endDate = null;
                if (!CommonUtil.isEmpty(startDateStr)) {
                    startDate = sdf.parse(startDateStr);
                }
                if (!CommonUtil.isEmpty(endDateStr)) {
                    // 设置结束日期为当天的23:59:59
                    endDate = sdf.parse(endDateStr);
                    endDate = new Date(endDate.getTime() + 24 * 60 * 60 * 1000 - 1);
                }
                totalCount = sysOperationLogService.findCountByDateRange(startDate, endDate);
                logs = sysOperationLogService.findByDateRange(startDate, endDate, offset, pageSize);
            } else if ("username".equals(type)) {
                totalCount = sysOperationLogService.findCountByUsername(data);
                logs = sysOperationLogService.findByUsername(data, offset, pageSize);
            } else if ("module".equals(type)) {
                totalCount = sysOperationLogService.findCountByModule(data);
                logs = sysOperationLogService.findByModule(data, offset, pageSize);
            } else if ("operationType".equals(type)) {
                totalCount = sysOperationLogService.findCountByOperationType(data);
                logs = sysOperationLogService.findByOperationType(data, offset, pageSize);
            }

            List<Map<String, Object>> lists = new ArrayList<>();
            if (!CommonUtil.isEmpty(logs) && logs.size() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                for (SysOperationLog log : logs) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("logId", log.getLogId());
                    map.put("username", log.getUsername());
                    map.put("roleName", log.getRoleName());
                    map.put("module", log.getModule());
                    map.put("operationType", log.getOperationType());
                    map.put("operationContent", log.getOperationContent());
                    map.put("operationTime", log.getOperationTime() != null ? sdf.format(log.getOperationTime()) : "");
                    map.put("status", log.getStatus() != null ? log.getStatus().intValue() : 0);
                    map.put("errorMsg", log.getErrorMsg());
                    lists.add(map);
                }
            }
            return JsonUtil.buildJsonByTotalCount(lists, totalCount);
        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }
}
