package com.hkd.controller.reception;

import com.hkd.pojo.SysNotification;
import com.hkd.service.SysNotificationService;
import com.hkd.util.CommonUtil;
import com.hkd.util.JsonUtil;
import com.hkd.util.PageQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 前台消息通知控制器
 */
@Controller
@RequestMapping("/reception/notification")
public class ReceptionNotificationController {

    @Autowired
    private SysNotificationService sysNotificationService;

    /**
     * 获取消息列表（分页）
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public String getNotificationList(HttpServletRequest request, HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("receptionUserId");
            if (CommonUtil.isEmpty(userId)) {
                return JsonUtil.buildFalseJson(401, "未登录！");
            }

            int page = Integer.parseInt(request.getParameter("page") != null ? request.getParameter("page") : "1");
            int pageSize = Integer.parseInt(request.getParameter("pageSize") != null ? request.getParameter("pageSize") : "10");

            PageQuery pageQuery = new PageQuery(page, pageSize);
            int offset = pageQuery.getOffset();

            int totalCount = sysNotificationService.countByUserId(userId);
            List<SysNotification> notifications = sysNotificationService.findByUserId(userId, offset, pageSize);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            List<Map<String, Object>> list = new ArrayList<>();
            if (notifications != null) {
                for (SysNotification n : notifications) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("notificationId", n.getNotificationId());
                    map.put("type", n.getType());
                    map.put("typeName", getTypeName(n.getType()));
                    map.put("title", n.getTitle());
                    map.put("content", n.getContent());
                    map.put("relatedId", n.getRelatedId());
                    map.put("isRead", n.getIsRead());
                    map.put("createTime", n.getCreateTime() != null ? sdf.format(n.getCreateTime()) : "");
                    list.add(map);
                }
            }
            //
            System.out.println("消息列表为："+list);
            return JsonUtil.buildJsonByTotalCount(list, totalCount);

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 获取未读消息数量
     */
    @RequestMapping(value = "/unread/count", method = RequestMethod.GET)
    @ResponseBody
    public String getUnreadCount(HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("receptionUserId");
            if (CommonUtil.isEmpty(userId)) {
                return JsonUtil.buildFalseJson(401, "未登录！");
            }

            int unreadCount = sysNotificationService.countUnreadByUserId(userId);

            Map<String, Object> data = new HashMap<>();
            data.put("unreadCount", unreadCount);

            return JsonUtil.objectToJson(data, "获取成功！");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 标记单条消息为已读
     */
    @RequestMapping(value = "/read", method = RequestMethod.POST)
    @ResponseBody
    public String markAsRead(@RequestParam("notificationId") Integer notificationId, HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("receptionUserId");
            if (CommonUtil.isEmpty(userId)) {
                return JsonUtil.buildFalseJson(401, "未登录！");
            }

            // 验证消息是否属于当前用户
            SysNotification notification = sysNotificationService.findById(notificationId);
            if (notification == null || !notification.getUserId().equals(userId)) {
                return JsonUtil.buildFalseJson(404, "消息不存在或无权操作！");
            }

            sysNotificationService.markAsRead(notificationId);

            return JsonUtil.objectToJson(null, "标记成功！");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 标记所有消息为已读
     */
    @RequestMapping(value = "/readAll", method = RequestMethod.POST)
    @ResponseBody
    public String markAllAsRead(HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("receptionUserId");
            if (CommonUtil.isEmpty(userId)) {
                return JsonUtil.buildFalseJson(401, "未登录！");
            }

            sysNotificationService.markAllAsRead(userId);

            return JsonUtil.objectToJson(null, "全部标记为已读！");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 删除消息
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String deleteNotification(@RequestParam("notificationId") Integer notificationId, HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("receptionUserId");
            if (CommonUtil.isEmpty(userId)) {
                return JsonUtil.buildFalseJson(401, "未登录！");
            }

            // 验证消息是否属于当前用户
            SysNotification notification = sysNotificationService.findById(notificationId);
            if (notification == null || !notification.getUserId().equals(userId)) {
                return JsonUtil.buildFalseJson(404, "消息不存在或无权操作！");
            }

            sysNotificationService.delete(notificationId);

            return JsonUtil.objectToJson(null, "删除成功！");

        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.buildFalseJson(500, e.getMessage());
        }
    }

    /**
     * 获取消息类型名称
     */
    private String getTypeName(String type) {
        if (type == null) return "系统消息";
        switch (type) {
            case "leave_approve":
                return "请假审批";
            case "supplement_approve":
                return "补卡审批";
            case "promotion":
                return "职位变更";
            case "demotion":
                return "职位变更";
            default:
                return "系统消息";
        }
    }
}
