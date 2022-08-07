package com.bitfye.common.base.util;

import com.bitfye.common.base.enums.LanguageEnum;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class LocaleUtils {

    private final MessageSource messageSource;

    public LocaleUtils(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String key, LanguageEnum languageEnum) {
        String[] params = languageEnum.getCode().split("-");
        return messageSource.getMessage(key, null, new Locale(params[0], params[1]));
    }
}
