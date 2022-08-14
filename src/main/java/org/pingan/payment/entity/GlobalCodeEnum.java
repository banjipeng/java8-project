package org.pingan.payment.entity;

import lombok.Data;

public enum GlobalCodeEnum {

    /**
     * 定义全局返回码
     */
    GL_SUCC_0000(0,"成功"),
    GL_FAIL_9996(996,"不支持的HttpMethod"),
    GL_FAIL_9997(997,"HTTP错误"),
    GL_FAIL_9998(998,"参数错误"),
    GL_FAIL_9999(999,"系统异常");

    private Integer code;
    private String desc;

    GlobalCodeEnum(Integer code,String desc){
        this.code = code;
        this.desc = desc;
    }
    /**
     * 根据编码获取枚举类型
     */
    public static GlobalCodeEnum getByCode(Integer code){
        if (code == null){
            return null;
        }
        GlobalCodeEnum[] values = GlobalCodeEnum.values();
        for (GlobalCodeEnum value : values) {
            if (value.getCode().equals(code)){
                return value;
            }
        }
        return null;
    }

    public Integer getCode(){
        return code;
    }

    public String getDesc(){
        return desc;
    }
}
