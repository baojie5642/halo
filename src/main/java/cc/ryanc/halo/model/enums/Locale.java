package cc.ryanc.halo.model.enums;

/**
 * @author : wangry
 * @version : 1.0
 * @date : 2018年09月08日
 */
public enum Locale {

    /**
     * 简体中文
     */
    ZH_CN("zh_CN"),

    /**
     * 英文
     */
    EN_US("en_US");

    private String value;

    Locale(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
