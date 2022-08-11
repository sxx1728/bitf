package com.bitfye.common.model.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author ming.jia
 * @version 1.0
 * @description 返回实体
 * @date 2021/11/19 上午10:00
 **/
@Data
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class BitfyeResponse<T>  {

    public static final String SUCCESS = "ok";

    @ApiModelProperty(value = "响应状态", allowableValues = "true,false", required = true)
    private Boolean success;

    @ApiModelProperty(value = "错误码", dataType = "string")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String code;

    @ApiModelProperty(value = "错误信息（英文）", dataType = "string")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;

    @ApiModelProperty(value = "返回数据-泛型，支持各种返回的数据格式类型", dataType = "object")
    private T data;

    public boolean isSuccess() {
        return success;
    }

    public static <T> TypeReference<BitfyeResponse<T>> type(Class<T> dataType) {
        return new TypeReference<BitfyeResponse<T>>() {
            @Override
            public Type getType() {
                TypeToken<?> typeToken = TypeToken.of(super.getType());
                return typeToken.where(new TypeParameter<T>() {
                }, dataType).getType();
            }
        };
    }

    public static <T> TypeReference<BitfyeResponse<List<T>>> listType(Class<T> dataType) {
        return new TypeReference<BitfyeResponse<List<T>>>() {
            @Override
            public Type getType() {
                TypeToken<?> typeToken = TypeToken.of(super.getType());
                return typeToken.where(new TypeParameter<T>() {
                }, dataType).getType();
            }
        };
    }
}
