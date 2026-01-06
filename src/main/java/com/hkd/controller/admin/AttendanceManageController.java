package com.hkd.controller.admin;

import com.hkd.pojo.AttendanceRecord;
import com.hkd.pojo.EmpInfo;
import com.hkd.service.AttendanceRecordService;
import com.hkd.service.EmpInfoService;
import com.hkd.util.CommonUtil;
import com.hkd.util.JsonUtil;
import com.hkd.util.PageQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 杨
 * @version 1.0
 * @date 2025/12/25 18:20
 * @description TODO: 考勤记录信息的管理
 */
@Controller
@RequestMapping("admin/attendanceManage")
public class AttendanceManageController {
    @Autowired
    private AttendanceRecordService attendanceRecordService;
    @Autowired
    private EmpInfoService empInfoService;

    @RequestMapping(value = "/data",method = RequestMethod.GET)
    @ResponseBody
    public String findAll(HttpServletRequest request){
        try{
            int offset = Integer.parseInt(request.getParameter("page"));
            int pageSize = Integer.parseInt(request.getParameter("pageSize"));
            PageQuery pageQuery = new PageQuery(offset,pageSize);
            offset = pageQuery.getOffset();
            pageSize = pageQuery.getPageSize();
            int totalCount=attendanceRecordService.findAllCount();
            List<Map<String,Object>> lists=new ArrayList<>();
            List<AttendanceRecord> records=attendanceRecordService.findAll(offset,pageSize);
            if(!CommonUtil.isEmpty(records) && records.size()>0){
                for(AttendanceRecord record:records){
                    Map<String,Object> map=new HashMap<>();
                    EmpInfo empInfo=empInfoService.findById(record.getEmpId());
                    map.put("recordId",record.getRecordId());
                    map.put("empNo",empInfo.getEmpNo());
                    map.put("checkDate",record.getCheckDate());
                    map.put("checkInTime",record.getCheckInTime());
                    map.put("status",record.getStatus());
                    map.put("remark",record.getRemark());
                    lists.add(map);
                }
                return  JsonUtil.buildJsonByTotalCount(lists,totalCount);
            }
            return JsonUtil.buildFalseJson(404,"获取数据失败！");
        }catch (Exception e){
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500,"数据获取失败");
        }
    }


    @RequestMapping(value = "/search",method = RequestMethod.GET)
    @ResponseBody
    public String findBymsg(HttpServletRequest request){
        try{
            String empNo=request.getParameter("empNo");
            int offset = Integer.parseInt(request.getParameter("page"));
            int pageSize = Integer.parseInt(request.getParameter("pageSize"));
            PageQuery pageQuery = new PageQuery(offset,pageSize);
            offset = pageQuery.getOffset();
            pageSize = pageQuery.getPageSize();
            EmpInfo empInfo1=empInfoService.findByNo(empNo);
            int empId=empInfo1.getEmpId();
            int totalCount=attendanceRecordService.findCountByEmpId(empId);
            List<Map<String,Object>> lists=new ArrayList<>();
            List<AttendanceRecord> records=attendanceRecordService.findById(empId,offset,pageSize);
            if(!CommonUtil.isEmpty(records) && records.size()>0){
                for (AttendanceRecord record : records) {
                    Map<String,Object> map=new HashMap<>();
                    map.put("recordId",record.getRecordId());
                    map.put("empNo",empNo);
                    map.put("checkDate",record.getCheckDate());
                    map.put("checkInTime",record.getCheckInTime());
                    map.put("status",record.getStatus());
                    map.put("remark",record.getRemark());
                    lists.add(map);
                }
                return  JsonUtil.buildJsonByTotalCount(lists,totalCount);
            }
            return JsonUtil.buildFalseJson(404,"获取数据失败！");
        }catch (Exception e){
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500,"数据获取失败");
        }
    }

    /**
     * 按日期范围搜索考勤记录
     */
    @RequestMapping(value = "/searchByDate", method = RequestMethod.GET)
    @ResponseBody
    public String searchByDate(HttpServletRequest request) {
        try {
            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");
            int offset = Integer.parseInt(request.getParameter("page"));
            int pageSize = Integer.parseInt(request.getParameter("pageSize"));
            PageQuery pageQuery = new PageQuery(offset, pageSize);
            offset = pageQuery.getOffset();
            pageSize = pageQuery.getPageSize();

            int totalCount = attendanceRecordService.findCountByDateRange(startDate, endDate);
            List<Map<String, Object>> lists = new ArrayList<>();
            List<AttendanceRecord> records = attendanceRecordService.findByDateRange(startDate, endDate, offset, pageSize);

            if (!CommonUtil.isEmpty(records) && records.size() > 0) {
                for (AttendanceRecord record : records) {
                    Map<String, Object> map = new HashMap<>();
                    EmpInfo empInfo = empInfoService.findById(record.getEmpId());
                    map.put("recordId", record.getRecordId());
                    map.put("empNo", empInfo != null ? empInfo.getEmpNo() : "未知");
                    map.put("checkDate", record.getCheckDate());
                    map.put("checkInTime", record.getCheckInTime());
                    map.put("status", record.getStatus());
                    map.put("remark", record.getRemark());
                    lists.add(map);
                }
                return JsonUtil.buildJsonByTotalCount(lists, totalCount);
            }
            return JsonUtil.buildJsonByTotalCount(new ArrayList<>(), 0);
        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, "数据获取失败");
        }
    }

}
