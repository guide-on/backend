package com.guideon.community.service;

import com.guideon.community.dto.*;

import java.util.Map;

public interface CommunityService {
    Long createPost(Long currentMemberId, PostCreateRequest req);
    Map<String, Object> listPosts(PostListRequest req);
    PostDetailResponse getPostDetail(Long postId);
    Map<String, Object> searchPosts(String keyword, PostListRequest req);
    void updatePost(Long currentMemberId, PostUpdateRequest req);
    void deletePost(Long currentMemberId, Long postId);

    // 추가 API
    // 댓글
    Long createComment(Long currentMemberId, CommentCreateRequest req);
    void updateComment(Long currentMemberId, CommentUpdateRequest req);
    void deleteComment(Long currentMemberId, Long commentId);

    // 토글
    boolean toggleLike(Long currentMemberId, Long postId);
    boolean toggleBookmark(Long currentMemberId, Long postId);

    // 인기글 & 추천
    Map<String, Object> popular(int size);
    Map<String, Object> recommend(Long currentMemberId, int size);
}
