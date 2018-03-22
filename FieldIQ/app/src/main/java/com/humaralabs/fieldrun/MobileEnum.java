package com.humaralabs.fieldrun;

/**
 * Created by pc1 on 09-12-2015.
 */
public enum MobileEnum {
    OLDVERSIONAPK(1),

    INVALIDCREDENTIALS(2),

    WRONGDATE(3),

    RELOGIIN(4),
    TOKENEXPIRED(5);


    private final int code;

    MobileEnum(int code){
        this.code = code;
    }

    public int getCode(){
        return code;
    }
}

