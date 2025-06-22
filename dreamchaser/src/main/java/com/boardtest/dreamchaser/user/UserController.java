package com.boardtest.dreamchaser.user;

import com.boardtest.dreamchaser.comment.Comment;
import com.boardtest.dreamchaser.comment.CommentService;
import com.boardtest.dreamchaser.post.Post;
import com.boardtest.dreamchaser.post.PostService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import java.security.Principal;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;
    private final PasswordEncoder passwordEncoder;


    @GetMapping("/signup")
    public String signup(Model model) {

        model.addAttribute("userCreateForm", new UserCreateForm());
        return "signup_form";
    }

    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "signup_form";
        }


        if (!userCreateForm.getPassword().equals(userCreateForm.getPasswordConfirm())) {
            bindingResult.rejectValue("passwordConfirm", "passwordInCorrect", "2개의 패스워드가 일치하지 않습니다.");
            return "signup_form";
        }


        try {
            userService.create(userCreateForm.getUsername(), userCreateForm.getPassword(), userCreateForm.getNickname());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
            return "signup_form";
        } catch (Exception e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", e.getMessage());
            return "signup_form";
        }

        return "redirect:/";
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        String errorMessage = null;


        if (session != null) {
            AuthenticationException ex = (AuthenticationException) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            if (ex != null) {
                errorMessage = ex.getMessage();
            }
        }



        model.addAttribute("errorMessage", errorMessage);

        return "login_form";
    }

    // ★★★ 마이페이지 요청을 처리하는 GET 메소드 ★★★
    @PreAuthorize("isAuthenticated()") // 로그인한 사용자만 접근 가능
    @GetMapping("/mypage")
    public String mypage(Model model, Principal principal) {
        // 1. 현재 로그인한 사용자의 User 객체를 가져온다.
        String username = principal.getName();
        User user = this.userService.getUser(username);

        // 2. 해당 사용자가 작성한 게시글 목록을 가져온다.
        List<Post> postList = this.postService.findByAuthor(user);

        // 3. 해당 사용자가 작성한 댓글 목록을 가져온다.
        List<Comment> commentList = this.commentService.findByAuthor(user);

        // 4. 모델에 모든 데이터를 담아 뷰로 전달한다.
        model.addAttribute("user", user);
        model.addAttribute("postList", postList);
        model.addAttribute("commentList", commentList);

        return "mypage"; // templates/mypage.html 템플릿을 보여줌

    }

    // ★★★ 회원 탈퇴 확인 페이지를 보여주는 메소드 ★★★
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/withdraw")
    public String withdraw() {
        return "withdraw_form";
    }

    // 2. 확인 페이지에서 '탈퇴하기' 버튼 클릭 시, 실제 탈퇴를 처리하는 POST 메소드
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/withdraw")
    public String withdraw(@RequestParam String password, Principal principal, HttpServletRequest request, HttpServletResponse response) {
        User user = this.userService.getUser(principal.getName());

        // 입력한 비밀번호와 저장된 비밀번호가 일치하는지 확인
        if (passwordEncoder.matches(password, user.getPassword())) {
            // 일치하면 사용자 삭제
            this.userService.delete(user);

            // 현재 사용자 강제 로그아웃 처리
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                new SecurityContextLogoutHandler().logout(request, response, auth);
            }

            // 메인 페이지로 리다이렉트
            return "redirect:/";
        } else {
            // 일치하지 않으면, 에러 파라미터와 함께 다시 탈퇴 폼으로 리다이렉트
            return "redirect:/user/withdraw?error=true";
        }
    }

}