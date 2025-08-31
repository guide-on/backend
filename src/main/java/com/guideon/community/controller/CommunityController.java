package com.guideon.community.controller;

import com.guideon.common.dto.CommonResponseDTO;
import com.guideon.common.exception.UnauthorizedException;
import com.guideon.community.dto.*;
import com.guideon.community.enums.PostSort;
import com.guideon.community.mapper.MemberLookupMapper;
import com.guideon.community.service.CommunityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community")
public class CommunityController {

    private final CommunityService communityService;
    private final MemberLookupMapper memberLookupMapper;

    private Long currentMemberId(Authentication auth) {
        if (auth == null) throw new UnauthorizedException("로그인이 필요합니다.");
        String email = auth.getName();
        Long memberId = memberLookupMapper.findMemberIdByEmail(email);
        if (memberId == null) throw new UnauthorizedException("회원 식별 실패");
        return memberId;
    }

    // 게시글
    @PostMapping("/posts")
    public CommonResponseDTO<Map<String, Object>> createPost(@RequestBody PostCreateRequest req, Authentication auth) {
        Long memberId = currentMemberId(auth);
        Long postId = communityService.createPost(memberId, req);
        return CommonResponseDTO.ok(Map.of("postId", postId));
    }

    @GetMapping("/posts")
    public CommonResponseDTO<Map<String, Object>> listPosts(@ModelAttribute PostListRequest req) {
        if (req.getSort() == null) req.setSort(PostSort.LATEST);
        return CommonResponseDTO.ok(communityService.listPosts(req));
    }

    @GetMapping("/posts/{postId}")
    public CommonResponseDTO<PostDetailResponse> getDetail(@PathVariable Long postId) {
        return CommonResponseDTO.ok(communityService.getPostDetail(postId));
    }

    @GetMapping("/posts/search")
    public CommonResponseDTO<Map<String, Object>> search(@RequestParam(required = false) String keyword,
                                                         @ModelAttribute PostListRequest req) {
        return CommonResponseDTO.ok(communityService.searchPosts(keyword, req));
    }

    @PutMapping("/posts/{postId}")
    public CommonResponseDTO<Map<String, Object>> update(@PathVariable Long postId,
                                                         @RequestBody PostUpdateRequest req,
                                                         Authentication auth) {
        Long memberId = currentMemberId(auth);
        req.setPostId(postId);
        communityService.updatePost(memberId, req);
        return CommonResponseDTO.ok(Map.of("postId", postId, "updated", true));
    }

    @DeleteMapping("/posts/{postId}")
    public CommonResponseDTO<Map<String, Object>> delete(@PathVariable Long postId, Authentication auth) {
        Long memberId = currentMemberId(auth);
        communityService.deletePost(memberId, postId);
        return CommonResponseDTO.ok(Map.of("postId", postId, "deleted", true));
    }

    // 댓글
    @PostMapping("/posts/{postId}/comments")
    public CommonResponseDTO<Map<String, Object>> createComment(@PathVariable Long postId,
                                                                @RequestBody CommentCreateRequest req,
                                                                Authentication auth) {
        Long memberId = currentMemberId(auth);
        req.setPostId(postId);
        Long id = communityService.createComment(memberId, req);
        return CommonResponseDTO.ok(Map.of("commentId", id));
    }

    @PutMapping("/comments/{commentId}")
    public CommonResponseDTO<Map<String, Object>> updateComment(@PathVariable Long commentId,
                                                                @RequestBody CommentUpdateRequest req,
                                                                Authentication auth) {
        Long memberId = currentMemberId(auth);
        req.setCommentId(commentId);
        communityService.updateComment(memberId, req);
        return CommonResponseDTO.ok(Map.of("commentId", commentId, "updated", true));
    }

    @DeleteMapping("/comments/{commentId}")
    public CommonResponseDTO<Map<String, Object>> deleteComment(@PathVariable Long commentId, Authentication auth) {
        Long memberId = currentMemberId(auth);
        communityService.deleteComment(memberId, commentId);
        return CommonResponseDTO.ok(Map.of("commentId", commentId, "deleted", true));
    }

    // 토글
    @PostMapping("/posts/{postId}/like/toggle")
    public CommonResponseDTO<Map<String, Object>> toggleLike(@PathVariable Long postId, Authentication auth) {
        Long memberId = currentMemberId(auth);
        boolean liked = communityService.toggleLike(memberId, postId);
        return CommonResponseDTO.ok(Map.of("postId", postId, "liked", liked));
    }

    @PostMapping("/posts/{postId}/bookmark/toggle")
    public CommonResponseDTO<Map<String, Object>> toggleBookmark(@PathVariable Long postId, Authentication auth) {
        Long memberId = currentMemberId(auth);
        boolean bookmarked = communityService.toggleBookmark(memberId, postId);
        return CommonResponseDTO.ok(Map.of("postId", postId, "bookmarked", bookmarked));
    }

    // 인기/추천
    @GetMapping("/posts/popular")
    public CommonResponseDTO<Map<String, Object>> popular(@RequestParam(defaultValue = "10") int size) {
        return CommonResponseDTO.ok(communityService.popular(size));
    }

    @GetMapping("/posts/recommend")
    public CommonResponseDTO<Map<String, Object>> recommend(@RequestParam(defaultValue = "10") int size, Authentication auth) {
        Long memberId = currentMemberId(auth);
        return CommonResponseDTO.ok(communityService.recommend(memberId, size));
    }
}
