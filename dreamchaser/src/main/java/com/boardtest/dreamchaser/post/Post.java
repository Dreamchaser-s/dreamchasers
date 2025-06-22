package com.boardtest.dreamchaser.post;

import com.boardtest.dreamchaser.category.Category;
import com.boardtest.dreamchaser.comment.Comment;
import com.boardtest.dreamchaser.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OrderBy;

@Getter
@Setter
@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createDate;



    @ManyToOne
    private User author;

    @Column(columnDefinition = "integer default 0", nullable = false)
    private int viewCount;

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @OrderBy("createDate ASC")
    private List<Comment> commentList = new ArrayList<>();

    @Enumerated(EnumType.STRING) // DB에 저장될 때 Enum의 이름을 문자열로 저장
    private Category category; // 예: "노트북", "스마트폰", "키보드" 등

    private int starRating; // 1 ~ 5점 사이의 별점

    @Column(length = 300)
    private String summary; // 한줄평

    @Column(columnDefinition = "TEXT")
    private String pros; // 장점 (리스트 형식으로 입력받을 수 있음)

    @Column(columnDefinition = "TEXT")
    private String cons; // 단점

    private String imageUrl; // 대표 이미지 URL

    @ManyToMany
    Set<User> voter; // 추천인. Set을 사용하면 중복 없이 저장 가능

    @Enumerated(EnumType.STRING)
    private PostStatus status;

}