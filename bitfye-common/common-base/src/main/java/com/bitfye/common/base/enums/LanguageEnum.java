package com.bitfye.common.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum LanguageEnum {

    CHINESE("zh-CN"),
    ENGLISH("en-US");

    final String code;

    public static LanguageEnum getInstance(String code) {
        return Arrays.stream(LanguageEnum.values())
                .filter(e -> StringUtils.hasText(code) && code.toLowerCase().contains(e.getCode().toLowerCase()))
                .findFirst()
                .orElse(null);
    }
}
