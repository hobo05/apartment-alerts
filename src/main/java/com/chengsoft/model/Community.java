package com.chengsoft.model;

/**
 * The various Avalon communities
 *
 * @author tcheng
 */
public enum Community {

    SOMERVILLE("MA039", "http://www.avaloncommunities.com/massachusetts/somerville-apartments/ava-somerville"),
    ASSEMBLY_ROW("MA038", "http://www.avaloncommunities.com/massachusetts/somerville-apartments/avalon-at-assembly-row");

    private String code;
    private String baseUrl;

    Community(String code, String baseUrl) {
        this.code = code;
        this.baseUrl = baseUrl;
    }

    public String getCode() {
        return code;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
