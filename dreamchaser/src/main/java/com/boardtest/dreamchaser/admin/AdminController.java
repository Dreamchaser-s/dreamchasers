package com.boardtest.dreamchaser.admin;

import com.boardtest.dreamchaser.post.Post;
import com.boardtest.dreamchaser.post.PostService;
import com.boardtest.dreamchaser.user.User;
import com.boardtest.dreamchaser.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@RequestMapping("/admin")
@RequiredArgsConstructor
@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final PostService postService;


    @GetMapping("/users")
    public String userList(Model model, @PageableDefault(size = 10, sort = "createDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<User> userPaging = userService.findUsers(pageable);
        model.addAttribute("userPaging", userPaging);
        return "admin/user_list";
    }


    @PostMapping("/users/ban/temp/{id}")
    public String banTemporarily(@PathVariable("id") Long id, @RequestParam(defaultValue = "7") int days, Principal principal) {
        User user = userService.getUserById(id);
        if (!user.getUsername().equals(principal.getName())) {
            userService.banTemporarily(user, days);
        }
        return "redirect:/admin/users";
    }


    @PostMapping("/users/ban/perm/{id}")
    public String banPermanently(@PathVariable("id") Long id, Principal principal) {
        User user = userService.getUserById(id);
        if (!user.getUsername().equals(principal.getName())) {
            userService.banPermanently(user);
        }
        return "redirect:/admin/users";
    }


    @PostMapping("/users/unban/{id}")
    public String unban(@PathVariable("id") Long id) {
        User user = userService.getUserById(id);
        userService.unban(user);
        return "redirect:/admin/users";
    }


    @GetMapping("/posts/pending")
    public String pendingList(Model model, @PageableDefault(size = 10, sort = "createDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Post> pendingPaging = this.postService.findPendingPosts(pageable);
        model.addAttribute("pendingPaging", pendingPaging);
        return "admin/pending_posts";
    }


    @PostMapping("/posts/approve/{id}")
    public String approvePost(@PathVariable("id") Long id) {
        postService.approvePost(id);
        return "redirect:/admin/posts/pending";
    }


    @PostMapping("/posts/reject/{id}")
    public String rejectPost(@PathVariable("id") Long id) {
        postService.rejectPost(id);
        return "redirect:/admin/posts/pending";
    }
}