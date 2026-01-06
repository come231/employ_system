package com.hkd.util;

import java.util.List;

// 分页结果类
public class PageResult<T> {
    private Long total;        // 总记录数
    private Integer pageNum;   // 当前页
    private Integer pageSize;  // 每页条数
    private Integer totalPage; // 总页数
    private List<T> list;      // 当前页数据

    // 构造时自动计算总页数
    public PageResult(Long total, Integer pageNum, Integer pageSize, List<T> list) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.list = list;
        this.totalPage = (int) Math.ceil((double) total / pageSize);
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}