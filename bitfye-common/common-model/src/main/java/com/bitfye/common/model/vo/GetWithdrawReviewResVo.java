package com.bitfye.common.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author ming.jia
 * @version 1.0
 * @description TODO
 * @date 2022/8/14 下午9:47
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetWithdrawReviewResVo implements Serializable {

    private static final long serialVersionUID = 8351283557179096847L;

    private boolean needManualReview;
    private boolean passedReview;
}
