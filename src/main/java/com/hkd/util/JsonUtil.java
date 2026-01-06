package com.hkd.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;
import java.util.Map;

public class JsonUtil {
	// 全局Gson实例
	private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();


	/**
	 * 通用对象转JSON（无嵌套）
	 */
	public static String toJson(Object obj) {
		return GSON.toJson(obj);
	}

	/**
	 * 统一返回格式：{code:0, msg:"", objects:[]}
	 */
	public static String objectToJson(Object obj,String msg) {
		Map<String, Object> result = new java.util.HashMap<>();
		result.put("code", "200");
		result.put("msg", msg);
		result.put("data", obj);
		return GSON.toJson(result);
	}

	/**
	 * 带总数的分页返回格式：{code:0, msg:"", count:总数, data:[]}
	 */
	public static String buildJsonByTotalCount(List list, int totalCount) {
		Map<String, Object> result = new java.util.HashMap<>();
		result.put("code", "200");
		result.put("msg", "处理成功");
		result.put("count", totalCount);
		result.put("data", list);
		return GSON.toJson(result);
	}

	/**
	 * 列表返回格式：{code:0, msg:"", count:长度, objects:[]}
	 */
	public static String buildJson(List list) {
		Map<String, Object> result = new java.util.HashMap<>();
		if (!CommonUtil.isEmpty(list)) {
			result.put("code", "200");
			result.put("msg", "处理成功");
		}
		result.put("count", list.size());
		result.put("data", list);
		return GSON.toJson(result);
	}

	/**
	 * 错误返回格式：{code:错误码, msg:"错误信息"}
	 */
	public static String buildFalseJson(int code, String message) {
		Map<String, Object> result = new java.util.HashMap<>();
		result.put("code", code);
		result.put("msg", message);
		return GSON.toJson(result);
	}

	public static String buildFalseJson2(int code, String message1, String message2) {
		Map<String, Object> result = new java.util.HashMap<>();
		result.put("code", code);
		result.put("url", message1);
		result.put("fileName", message2);
		return GSON.toJson(result);
	}

	public static String buildCheckJarJson(Boolean result, String detail, String content) {
		Map<String, Object> resultMap = new java.util.HashMap<>();
		resultMap.put("result", result);
		resultMap.put("detail", detail);
		resultMap.put("content", content);
		return GSON.toJson(resultMap);
	}

	public static String buildSuccessJson(int code, String message) {
		Map<String, Object> result = new java.util.HashMap<>();
		result.put("code", code);
		result.put("msg", message);
		return GSON.toJson(result);
	}

	public static String MapToJson(Map<String, Object> m) {
		return GSON.toJson(m);
	}
}