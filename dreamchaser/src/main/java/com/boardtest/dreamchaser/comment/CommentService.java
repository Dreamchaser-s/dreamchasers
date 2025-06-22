package com.boardtest.dreamchaser.comment;

import com.boardtest.dreamchaser.post.Post;
import com.boardtest.dreamchaser.user.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    public void create(Post post, String content, User author, Comment parent) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setCreateDate(LocalDateTime.now());
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setParent(parent);
        this.commentRepository.save(comment);
    }

    @Transactional
    public Comment getComment(Long id) {
        Optional<Comment> comment = this.commentRepository.findById(id);
        if (comment.isPresent()) {
            return comment.get();
        } else {
            throw new RuntimeException("Comment not found");
        }
    }


    public void delete(Comment comment) {
        this.commentRepository.delete(comment);
    }

    public List<Comment> findByAuthor(User author) {
        return this.commentRepository.findByAuthor(author);
    }


    public Page<Comment> getCommentPage(Post post, Pageable pageable) {
        return commentRepository.findByPostAndParentIsNull(post, pageable);
    }

    public long getCommentCount(Post post) {
        return commentRepository.countByPost(post);
    }


}
