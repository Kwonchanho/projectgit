package com.woori.studylogin.DTO;

import com.woori.studylogin.Constant.CategoryType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class BoardDTO {
    private Integer id; //일련번호

    private String title; // 글 제목
    private String content; // 글 내용
    private String author; //작성자
    private Integer viewCount;
    private Integer likeCount; //추천
    private LocalDateTime modDate;
    private CategoryType category;
    private String boardImg;
    private LocalDateTime regDate;
}
