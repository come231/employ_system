package com.hkd.util;

// 分页参数类
public class PageQuery {
    private Integer pageNum = 1;  // 默认第1页
    private Integer pageSize = 10;// 默认每页10条

    // 计算MySQL分页偏移量，必须保证pageNum≥1
    public Integer getOffset() {
        if (pageNum < 1) pageNum = 1;
        return (pageNum - 1) * pageSize;
    }

    public PageQuery(Integer pageNum, Integer pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public PageQuery() {
    }

    // getter、setter
    public Integer getPageNum() { return pageNum; }
    public void setPageNum(Integer pageNum) { this.pageNum = pageNum; }
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
}