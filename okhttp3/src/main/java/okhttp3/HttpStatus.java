package okhttp3;

import java.net.HttpURLConnection;

public enum HttpStatus {
    // 消息
    CONTINUE(100, "Continue", "继续"),
    SWITCHING_PROTOCOLS(101, "Switching Protocol", "交换协议"),
    PROCESSING(102, "Processing", "处理"),
    // 成功
    OK(HttpURLConnection.HTTP_OK, "OK", "OK"),
    CREATED(HttpURLConnection.HTTP_CREATED, "Created", "创建"),
    ACCEPTED(HttpURLConnection.HTTP_ACCEPTED, "Accepted", "已接受"),
    NON_AUTHORITATIVE_INFORMATION(HttpURLConnection.HTTP_NOT_AUTHORITATIVE, "Non-Authoritative Information", "非授权信息"),
    NO_CONTENT(HttpURLConnection.HTTP_NO_CONTENT, "No Content", "无内容"),
    RESET_CONTENT(HttpURLConnection.HTTP_RESET, "Reset Content", "无内容"),
    PARTIAL_CONTENT(HttpURLConnection.HTTP_PARTIAL, "Partial Content", "部分内容"),
    MULTI_STATUS(207, "Multi-Status", "多状态"),
    ALREADY_REPORTED(208, "Already Reported", "已报告"),
    IM_USED(226, "IM Used", "IM Used"),
    // 重定向
    MULTIPLE_CHOICES(HttpURLConnection.HTTP_MULT_CHOICE, "Multiple Choice", "多种选择"),
    MOVED_PERMANENTLY(HttpURLConnection.HTTP_MOVED_PERM, "Moved Permanently", "永久移动"),
    MOVE_TEMPORARILY(HttpURLConnection.HTTP_MOVED_TEMP, "Found", "发现"),
    SEE_OTHER(HttpURLConnection.HTTP_SEE_OTHER, "See Other", "查看其它"),
    NOT_MODIFIED(HttpURLConnection.HTTP_NOT_MODIFIED, "Not Modified", "未修改"),
    USE_PROXY(HttpURLConnection.HTTP_USE_PROXY, "Use Proxy", "使用代理"),
    @Deprecated
    SWITCH_PROXY(306, "Switch Proxy", "开关代理"),// 在最新版的规范中，306状态码已经不再被使用。
    TEMPORARY_REDIRECT(307, "Temporary Redirect", "临时重定向"),
    PERMANENT_REDIRECT(308, "Permanent Redirect", "永久重定向"),
    // 请求错误
    BAD_REQUEST(HttpURLConnection.HTTP_BAD_REQUEST, "Bad Request", "错误的请求"),
    UNAUTHORIZED(HttpURLConnection.HTTP_UNAUTHORIZED, "Unauthorized", "未授权"),
    PAYMENT_REQUIRED(HttpURLConnection.HTTP_PAYMENT_REQUIRED, "Payment Required", "需要付费"),
    FORBIDDEN(HttpURLConnection.HTTP_FORBIDDEN, "Forbidden", "拒绝访问"),
    NOT_FOUND(HttpURLConnection.HTTP_NOT_FOUND, "Not Found", "未找到"),
    METHOD_NOT_ALLOWED(HttpURLConnection.HTTP_BAD_METHOD, "Method Not Allowed", "方法不允许"),
    NOT_ACCEPTABLE(HttpURLConnection.HTTP_NOT_ACCEPTABLE, "Not Acceptable", "不可接受"),
    PROXY_AUTHENTICATION_REQUIRED(HttpURLConnection.HTTP_PROXY_AUTH, "Proxy Authentication Required", "代理服务器需要身份验证"),
    REQUEST_TIMEOUT(HttpURLConnection.HTTP_CLIENT_TIMEOUT, "Request Timeout", "请求超时"),
    CONFLICT(HttpURLConnection.HTTP_CONFLICT, "Conflict", "冲突"),
    GONE(HttpURLConnection.HTTP_GONE, "Gone", "完成"),
    LENGTH_REQUIRED(HttpURLConnection.HTTP_LENGTH_REQUIRED, "Length Required", "需要长度"),
    PRECONDITION_FAILED(HttpURLConnection.HTTP_PRECON_FAILED, "Precondition Failed", "前提条件失败"),
    REQUEST_ENTITY_TOO_LARGE(HttpURLConnection.HTTP_ENTITY_TOO_LARGE, "Payload Too Large", "负载过大"),
    REQUEST_URI_TOO_LONG(HttpURLConnection.HTTP_REQ_TOO_LONG, "URI Too Long", "太长"),
    UNSUPPORTED_MEDIA_TYPE(HttpURLConnection.HTTP_UNSUPPORTED_TYPE, "Unsupported Media Type", "不支持的媒体类型"),
    REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable", "范围不合适"),
    EXPECTATION_FAILED(417, "Expectation Failed", "预期失败"),
    I_M_A_TEAPOT(418, "I'm a teapot", "我是一个茶壶"),
    MISDIRECTED_REQUEST(421, "Misdirected Request", "误导请求"),
    UNPROCESSABLE_ENTITY(422, "Unprocessable Entity", "无法处理的实体"),
    LOCKED(423, "Locked", "锁定"),
    FAILED_DEPENDENCY(424, "Failed Dependency", "依赖失败"),
    UPGRADE_REQUIRED(426, "Upgrade Required", "升级所需"),
    PRECONDITION_REQUIRED(428, "Precondition Required", "先决条件所需"),
    TOO_MANY_REQUESTS(429, "Too Many Requests", "请求量太大"),
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large", "请求头字段太大"),
    UNAVAILABLE_FOR_LEGAL_REASONS(451, "Unavailable For Legal Reasons", "由于法律原因无效"),
    // 服务器错误
    INTERNAL_SERVER_ERROR(HttpURLConnection.HTTP_INTERNAL_ERROR, "Internal Server Error", "内部服务器错误"),
    NOT_IMPLEMENTED(HttpURLConnection.HTTP_NOT_IMPLEMENTED, "Not Implemented", "服务器不支持"),
    BAD_GATEWAY(HttpURLConnection.HTTP_BAD_GATEWAY, "Bad Gateway", "错误的网关"),
    SERVICE_UNAVAILABLE(HttpURLConnection.HTTP_UNAVAILABLE, "Service Unavailable", "服务不可用"),
    GATEWAY_TIMEOUT(HttpURLConnection.HTTP_GATEWAY_TIMEOUT, "Gateway Timeout", "网关超时"),
    HTTP_VERSION_NOT_SUPPORTED(HttpURLConnection.HTTP_VERSION, "HTTP Version Not Supported", "服务器不支持当前HTTP版本"),
    VARIANT_ALSO_NEGOTIATES(506, "Variant Also Negotiates", "变体也进行协商"),
    INSUFFICIENT_STORAGE(507, "Insufficient Storage", "存储空间不足"),
    LOOP_DETECTED(508, "Loop Detected", "检测到循环"),
    NOT_EXTENDED(510, "Not Extended", "条件不满足"),
    NETWORK_AUTHENTICATION_REQUIRED(511, "Network Authentication Required", "网络需要身份验证");

    private final int code;
    private final String description;
    private final String translation;

    HttpStatus(int code, String description, String translation) {
        this.code = code;
        this.description = description;
        this.translation = translation;
    }

    public int code() {
        return code;
    }

    public String description() {
        return description;
    }

    public String translation() {
        return translation;
    }
}
