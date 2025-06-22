package com.boardtest.dreamchaser.post;

import com.boardtest.dreamchaser.category.Category;
import com.boardtest.dreamchaser.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;


import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {


    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.commentList c " +
            "LEFT JOIN FETCH c.author " +
            "LEFT JOIN FETCH c.children " +
            "WHERE p.id = :id AND c.parent IS NULL")
    Optional<Post> findByIdWithRootCommentsAndAllDetails(@Param("id") Long id);
    List<Post> findByAuthor(User author);
    Page<Post> findByCategoryAndStatus(Category category, PostStatus status, Pageable pageable);
    @Query("SELECT p FROM Post p WHERE p.status = :status AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%)")
    Page<Post> findByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") PostStatus status, Pageable pageable);
    List<Post> findTop6ByStatusOrderByCreateDateDesc(PostStatus status);
    @Query("SELECT p FROM Post p WHERE p.status = :status ORDER BY SIZE(p.voter) DESC, p.createDate DESC")
    Page<Post> findAllByStatusOrderByVoterDesc(@Param("status") PostStatus status, Pageable pageable);
    @Query("SELECT p FROM Post p WHERE p.status = :status ORDER BY SIZE(p.voter) ASC, p.createDate DESC")
    Page<Post> findAllByStatusOrderByVoterAsc(@Param("status") PostStatus status, Pageable pageable);
    Page<Post> findByStatus(PostStatus status, Pageable pageable);

}