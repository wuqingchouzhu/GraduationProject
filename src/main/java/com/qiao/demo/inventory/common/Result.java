package com.qiao.demo.inventory.common;

/**
 * 统一的 API 响应结果封装类
 */
public class Result<T> {
    private Integer code; // 状态码：200代表成功，500代表失败
    private String message; // 提示信息
    private T data; // 真正要返回的数据（比如商品列表）

    // 成功时的快捷方法
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    // 失败时的快捷方法
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    // Getter 和 Setter
    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}