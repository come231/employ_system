package com.hkd.pojo;

import java.util.Date;

/**
 * @author 杨
 * @version 1.0
 * @date 2025/12/25 19:01
 * @description TODO: 类功能描述
 */
public class SysUserRoleQuery {
    private int userId;
    private Integer empId;//员工Id
    private String username;//账号
    private String password;
    private String roleName;//角色名称
    private String avatar;//头像地址
    private Date lastLoginTime;//上次登录时间

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Integer getEmpId() {
        return empId;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
}
