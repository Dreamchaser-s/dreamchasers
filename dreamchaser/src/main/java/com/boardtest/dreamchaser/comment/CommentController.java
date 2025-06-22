
package com.boardtest.dreamchaser.comment;

import com.boardtest.dreamchaser.post.Post;
import com.boardtest.dreamchaser.post.PostService;
import com.boardtest.dreamchaser.user.User;
import com.boardtest.dreamchaser.user.UserRole;
import com.boardtest.dreamchaser.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@RequestMapping("/comment")
@RequiredArgsConstructor
@Controller
public class CommentController {

    private final PostService postService;
    private final UserService userService;
    private final CommentService commentService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/{id}")
    public String createComment(@PathVariable("id") Long id,
                                @Valid CommentForm commentForm, // @RequestParam 대신 CommentForm 사용
                                BindingResult bindingResult, // 추가
                                Principal principal,
                                Model model, // Model 추가
                                RedirectAttributes redirectAttributes) { // RedirectAttributes 추가

        Post post = this.postService.getPost(id);

        // 유효성 검사 실패 시
        if (bindingResult.hasErrors()) {
            model.addAttribute("post", post); // Post 객체를 다시 모델에 추가
            model.addAttribute("commentForm", commentForm); // CommentForm을 다시 모델에 추가 (입력값 유지)
            return "post_detail"; // 유효성 검사 실패 시 post_detail 페이지로 이동
        }

        // 댓글 개수 제한 로직
        if (this.commentService.getCommentCount(post) >= 50) {
            redirectAttributes.addFlashAttribute("errorMessage", "댓글 개수가 50개를 초과하여 더 이상 댓글을 작성할 수 없습니다.");
            return String.format("redirect:/post/detail/%s", id);
        }

        User author = this.userService.getUser(principal.getName());

        Comment parent = null;
        if (commentForm.getParentId() != null) {
            parent = this.commentService.getComment(commentForm.getParentId());
        }

        this.commentService.create(post, commentForm.getContent(), author, parent);

        return String.format("redirect:/post/detail/%s", id);
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String commentDelete(@PathVariable("id") Long id, Principal principal) {
        Comment comment = this.commentService.getComment(id);
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(UserRole.ADMIN.getKey()));
        if (!isAdmin && !comment.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제 권한이 없습니다.");
        }

        Long postId = comment.getPost().getId();
        this.commentService.delete(comment);
        return String.format("redirect:/post/detail/%s", postId);
    }


}