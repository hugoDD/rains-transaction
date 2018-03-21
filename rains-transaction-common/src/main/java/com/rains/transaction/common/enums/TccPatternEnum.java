

package com.rains.transaction.common.enums;

/**
 * The enum Tcc pattern enum.
 * <p>
 * 当模式为tcc时候，在try异常中，会执行cancel方法，cc模式不会执行
 *
 * @author hugosz
 */
public enum TccPatternEnum {

    /**
     * Tcc tcc pattern enum.
     */
    TCC(1, "try,confirm,cancel模式"),

    /**
     * Cc tcc pattern enum.
     */
    CC(2, "confirm,cancel模式");

    private Integer code;

    private String desc;

    TccPatternEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * Gets code.
     *
     * @return the code
     */
    public Integer getCode() {
        return code;
    }

    /**
     * Sets code.
     *
     * @param code the code
     */
    public void setCode(Integer code) {
        this.code = code;
    }

    /**
     * Gets desc.
     *
     * @return the desc
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Sets desc.
     *
     * @param desc the desc
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }
}
