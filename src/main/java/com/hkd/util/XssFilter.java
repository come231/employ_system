package com.hkd.util;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * XSS防护过滤器（优化：减少过度过滤，仅过滤危险标签/脚本）
 */
public class XssFilter implements Filter {

    // 定义XSS攻击的正则表达式（仅过滤危险脚本，保留合法字符）
    private static Pattern[] patterns = new Pattern[]{
            // Script fragments（过滤<script>标签）
            Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // eval(...)
            Pattern.compile("eval\\s*\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // expression(...)
            Pattern.compile("expression\\s*\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // javascript:/vbscript: 伪协议
            Pattern.compile("(javascript|vbscript):", Pattern.CASE_INSENSITIVE),
            // 事件处理器（onclick/onload/onerror等）
            Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
    };

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper((HttpServletRequest) request);
        chain.doFilter(xssRequest, response);
    }

    @Override
    public void destroy() {
        // 销毁
    }

    /**
     * 清理XSS攻击字符串（优化：仅过滤危险内容，保留合法参数）
     */
    private String cleanXSS(String value) {
        if (value == null) {
            return null;
        }

        String cleanValue = value;
        for (Pattern pattern : patterns) {
            cleanValue = pattern.matcher(cleanValue).replaceAll("");
        }

        // 仅转义HTML特殊字符，不删除正常字符串
        cleanValue = cleanValue.replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("'", "&#39;")
                .replaceAll("\"", "&quot;");

        return cleanValue;
    }

    /**
     * XSS请求包装类
     */
    private class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

        public XssHttpServletRequestWrapper(HttpServletRequest servletRequest) {
            super(servletRequest);
        }

        @Override
        public String[] getParameterValues(String parameter) {
            String[] values = super.getParameterValues(parameter);
            if (values == null) {
                return null;
            }
            int count = values.length;
            String[] encodedValues = new String[count];
            for (int i = 0; i < count; i++) {
                encodedValues[i] = cleanXSS(values[i]);
            }
            return encodedValues;
        }

        @Override
        public String getParameter(String parameter) {
            String value = super.getParameter(parameter);
            return cleanXSS(value);
        }

        @Override
        public String getHeader(String name) {
            String value = super.getHeader(name);
            return cleanXSS(value);
        }
    }
}