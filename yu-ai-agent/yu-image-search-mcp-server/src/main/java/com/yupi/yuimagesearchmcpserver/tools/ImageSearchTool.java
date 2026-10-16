package com.yupi.yuimagesearchmcpserver.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基于 Unsplash API 的图片搜索工具（画质最好，适合追求图片质量场景）
 *
 * 获取 Access Key 的完整步骤（重要：注册账号 != 有 Key）：
 *   1. 打开 https://unsplash.com/oauth/applications
 *   2. 点击 New Application，勾选协议并填写应用名 / 描述
 *   3. 提交后进入应用详情页，下拉到 Keys 区域，复制 "Access Key"
 *   4. Demo 应用 50 次/小时；点 Apply for Production 通过后 5000 次/小时
 *
 * 合规要求（Unsplash API Guidelines，务必遵守，否则可能被封 Key）：
 *   - 必须直接使用 API 返回的图片 URL（热链接），不要下载后自行托管
 *   - 不要去除 URL 中的 ixid 参数
 *   - 应用需要为摄影师和 Unsplash 提供署名
 *   - 每次触发下载动作时，需调用 GET /photos/:id/download 上报（仅计数用）
 */
@Service
public class ImageSearchTool {

    // 替换为你的 Unsplash Access Key（不是 Secret Key）
    private static final String ACCESS_KEY = "Fz4YSid4Dz9l9uL1wASqqFHS35Bz67OfF2u1EEBiBi4";

    private static final String API_URL = "https://api.unsplash.com/search/photos";

    @Tool(description = "search image from web")
    public String searchImage(@ToolParam(description = "Search query keyword") String query) {
        try {
            return String.join(",", searchMediumImages(query));
        } catch (Exception e) {
            return "Error search image: " + e.getMessage();
        }
    }

    /**
     * 搜索中等尺寸的图片列表，返回 regular 尺寸的图片 URL（宽 1080px）
     *
     * Unsplash 关键参数：
     * query          - 搜索关键词（必填）
     * per_page       - 每页数量，默认 10，最大 30
     * page           - 页码，默认 1
     * orientation    - landscape / portrait / squarish
     * content_filter - low（默认）/ high；面向全年龄的应用建议 high
     * color          - 按颜色筛选，如 blue、black_and_white
     * order_by       - relevant（默认）/ latest
     *
     * @param query 搜索关键词
     * @return 图片 URL 列表
     */
    public List<String> searchMediumImages(String query) {
        Map<String, String> headers = new HashMap<>();
        // 认证头格式为 "Client-ID 你的AccessKey"，注意中间必须有空格
        headers.put("Authorization", "Client-ID " + ACCESS_KEY);
        headers.put("Accept-Version", "v1");

        Map<String, Object> params = new HashMap<>();
        params.put("query", query);
        params.put("per_page", 10);
        params.put("orientation", "landscape");
        params.put("content_filter", "high"); // 内容安全过滤，避免搜出不适宜图片

        String response = HttpUtil.createGet(API_URL)
                .addHeaders(headers)
                .form(params)
                .execute()
                .body();

        // 响应结构：{ "total": n, "total_pages": n, "results": [ { "urls": { "regular": "..." } } ] }
        JSONObject root = JSONUtil.parseObj(response);
        JSONArray results = root.getJSONArray("results");
        if (results == null) {
            return List.of();
        }
        return results.stream()
                .map(item -> (JSONObject) item)
                .map(item -> item.getJSONObject("urls"))
                .filter(urls -> urls != null)
                .map(urls -> urls.getStr("regular"))
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }

    /**
     * 带摄影师署名的搜索结果（满足 Unsplash 署名要求，推荐在真实产品中使用）
     *
     * @param query 搜索关键词
     * @return 形如 "图片URL | Photo by 摄影师名 on Unsplash (主页链接)" 的列表
     */
    public List<String> searchImagesWithAttribution(String query) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Client-ID " + ACCESS_KEY);
        headers.put("Accept-Version", "v1");

        Map<String, Object> params = new HashMap<>();
        params.put("query", query);
        params.put("per_page", 10);
        params.put("content_filter", "high");

        String response = HttpUtil.createGet(API_URL)
                .addHeaders(headers)
                .form(params)
                .execute()
                .body();

        JSONArray results = JSONUtil.parseObj(response).getJSONArray("results");
        if (results == null) {
            return List.of();
        }
        return results.stream()
                .map(item -> (JSONObject) item)
                .map(item -> {
                    JSONObject urls = item.getJSONObject("urls");
                    JSONObject user = item.getJSONObject("user");
                    String imgUrl = (urls == null) ? null : urls.getStr("regular");
                    if (StrUtil.isBlank(imgUrl)) {
                        return null;
                    }
                    String photographer = (user == null) ? "Unknown" : user.getStr("name");
                    String profile = (user != null && user.getJSONObject("links") != null)
                            ? user.getJSONObject("links").getStr("html")
                            : "https://unsplash.com";
                    // Unsplash 要求：署名摄影师 + 标注来源 Unsplash
                    return imgUrl + " | Photo by " + photographer + " on Unsplash (" + profile + ")";
                })
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }
}
