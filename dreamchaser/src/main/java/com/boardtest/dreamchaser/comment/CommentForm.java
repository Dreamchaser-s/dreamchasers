
package com.boardtest.dreamchaser.comment;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentForm {
    @NotEmpty(message = "내용은 필수항목입니다.")
    @Size(max = 1000, message = "내용은 1000자 이하로 작성해주세요.")
    private String content;

    private Long parentId;
}