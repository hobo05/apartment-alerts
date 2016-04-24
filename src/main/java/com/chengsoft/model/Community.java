package com.chengsoft.model;

/**
 * The various Avalon communities
 *
 * @author tcheng
 */
public enum Community {

    SOMERVILLE("MA039"),
    ASSEMBLY_ROW("MA038"),
    NORTH_POINT("MAD01");

    private String code;

    Community(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
