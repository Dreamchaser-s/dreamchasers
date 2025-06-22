package com.boardtest.dreamchaser.post;

import com.boardtest.dreamchaser.category.Category;
import com.boardtest.dreamchaser.user.User;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;

    @Value("${custom.upload.path}")
    private String uploadPath;


    public Page<Post> getList(Pageable pageable, String sortCode) {
        switch (sortCode) {
            case "voter_desc":
                return postRepository.findAllByStatusOrderByVoterDesc(PostStatus.APPROVED, pageable);
            case "voter_asc":
                return postRepository.findAllByStatusOrderByVoterAsc(PostStatus.APPROVED, pageable);
            case "view_count_desc":
                pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "viewCount"));
                break;
            case "view_count_asc":
                pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.ASC, "viewCount"));
                break;
            default:
                pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createDate"));
                break;
        }
        return postRepository.findByStatus(PostStatus.APPROVED, pageable);
    }


    public List<Post> getRecentList(int count) {
        return this.postRepository.findTop6ByStatusOrderByCreateDateDesc(PostStatus.APPROVED);
    }

    @Transactional // 이 어노테이션은 필수
    public Post getPost(Long id) {
        Optional<Post> post = this.postRepository.findByIdWithRootCommentsAndAllDetails(id); // ★★★ 이 줄 변경 ★★★
        if (post.isPresent()) {
            return post.get();
        } else {
            throw new RuntimeException("Post not found");
        }
    }

    // ★★★ 생성 시 기본 상태를 PENDING으로 설정 ★★★
    public void create(String title, String content, User author, Category category, Integer starRating, String summary,
                       String pros, String cons, MultipartFile imageFile) throws IOException {
        Post p = new Post();
        p.setTitle(title);
        p.setCreateDate(LocalDateTime.now());
        p.setAuthor(author);
        p.setCategory(category);
        p.setStarRating(starRating);
        p.setSummary(summary);
        p.setPros(pros);
        p.setCons(cons);
        p.setStatus(PostStatus.PENDING); // 기본 상태는 '승인 대기'

        String sanitizedContent = Jsoup.clean(content, "http://localhost:8080", Safelist.relaxed());
        p.setContent(sanitizedContent);

        String uniqueFilename = this.saveFile(imageFile);
        p.setImageUrl(uniqueFilename);

        this.postRepository.save(p);
    }

    // modify 메소드는 관리자가 내용을 수정할 수도 있으므로, 상태 변경 로직은 별도 메소드로 분리
    public void modify(Post post, String title, String content, Category category, Integer starRating, String summary,
                       String pros, String cons, MultipartFile imageFile) throws IOException {
        post.setTitle(title);
        post.setCategory(category);
        post.setStarRating(starRating);
        post.setSummary(summary);
        post.setPros(pros);
        post.setCons(cons);

        String sanitizedContent = Jsoup.clean(content, "http://localhost:8080", Safelist.relaxed());
        post.setContent(sanitizedContent);

        String uniqueFilename = this.saveFile(imageFile);
        if (uniqueFilename != null) {
            post.setImageUrl(uniqueFilename);
        }

        this.postRepository.save(post);
    }

    @Transactional
    public void incrementViewCount(Long id) {
        Post post = getPost(id);
        post.setViewCount(post.getViewCount() + 1);
        this.postRepository.save(post);
    }

    public void delete(Post post) {
        this.postRepository.delete(post);
    }

    @Transactional
    public void vote(Post post, User user) {
        if (post.getVoter() == null) {
            post.setVoter(new HashSet<>());
        }
        if (post.getVoter().contains(user)) {
            post.getVoter().remove(user);
        } else {
            post.getVoter().add(user);
        }
        this.postRepository.save(post);
    }

    // ★★★ '승인된' 글 중에서만 검색하도록 수정 ★★★
    public Page<Post> search(String keyword, Pageable pageable, String sortCode) {
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createDate"));
        return postRepository.findByKeywordAndStatus(keyword, PostStatus.APPROVED, pageable);
    }

    // 마이페이지용: 모든 상태의 글을 찾아야 하므로 수정 없음
    public List<Post> findByAuthor(User author) {
        return this.postRepository.findByAuthor(author);
    }

    // ★★★ '승인된' 글 중에서만 카테고리별로 찾도록 수정 ★★★
    public Page<Post> findByCategory(Category category, Pageable pageable, String sortCode) {
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createDate"));
        return postRepository.findByCategoryAndStatus(category, PostStatus.APPROVED, pageable);
    }

    // ★★★ 관리자 기능용 메소드들 추가 ★★★
    public Page<Post> findPendingPosts(Pageable pageable) {
        return postRepository.findByStatus(PostStatus.PENDING, pageable);
    }

    public void approvePost(Long id) {
        Post post = this.getPost(id);
        post.setStatus(PostStatus.APPROVED);
        this.postRepository.save(post);
    }

    public void rejectPost(Long id) {
        Post post = this.getPost(id);
        post.setStatus(PostStatus.REJECTED);
        this.postRepository.save(post);
    }


    public String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        File saveFile = new File(uploadPath, uniqueFilename);
        file.transferTo(saveFile);
        return uniqueFilename;
    }
}
