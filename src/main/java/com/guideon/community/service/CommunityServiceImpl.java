package com.guideon.community.service;

import com.guideon.community.domain.Comment;
import com.guideon.community.domain.Hashtag;
import com.guideon.community.domain.Post;
import com.guideon.community.domain.PostImage;
import com.guideon.community.dto.*;
import com.guideon.community.enums.FreeType;
import com.guideon.community.enums.HashtagType;
import com.guideon.community.enums.PostCategory;
import com.guideon.community.enums.PostSort;
import com.guideon.community.mapper.CommunityMapper;
import com.guideon.common.exception.BadRequestException;
import com.guideon.community.exception.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommunityServiceImpl implements CommunityService {

    private final CommunityMapper communityMapper;

    // ---------- 유틸: 검증 ----------
    private void validateCategoryAndTags(PostCategory category, FreeType freeType, List<Long> hashtagIds) {
        if (category == PostCategory.FREE && freeType == null) {
            throw new IllegalArgumentException("FREE 게시글은 freeType이 필요합니다.");
        }
        if (category == PostCategory.CASE && freeType != null) {
            throw new IllegalArgumentException("CASE 게시글은 freeType을 사용할 수 없습니다.");
        }
        if (hashtagIds == null || hashtagIds.isEmpty()) return;

        List<Hashtag> tags = communityMapper.selectHashtagsByIds(hashtagIds);
        if (tags.size() != hashtagIds.size()) {
            throw new IllegalArgumentException("존재하지 않는 해시태그가 포함되어 있습니다.");
        }
        for (Hashtag tag : tags) {
            HashtagType t = tag.getTagType();
            if (category == PostCategory.CASE && !(t == HashtagType.SECTOR || t == HashtagType.GENERIC)) {
                throw new IllegalArgumentException("CASE 게시글에는 SECTOR/GENERIC 태그만 허용됩니다.");
            }
            if (category == PostCategory.FREE && !(t == HashtagType.POST_TYPE || t == HashtagType.GENERIC)) {
                throw new IllegalArgumentException("FREE 게시글에는 POST_TYPE/GENERIC 태그만 허용됩니다.");
            }
        }
    }

    private void ensurePostTypeHashtagSynced(PostCategory category, FreeType freeType, List<Long> hashtagIds) {
        if (category != PostCategory.FREE || freeType == null) return;
        Hashtag postTypeTag = communityMapper.selectPostTypeHashtagByCode(freeType.name());
        if (postTypeTag == null) {
            throw new IllegalStateException("POST_TYPE 해시태그 시드가 없습니다: " + freeType.name());
        }
        if (!hashtagIds.contains(postTypeTag.getId())) {
            hashtagIds.add(postTypeTag.getId());
        }
    }

    private String sortKey(PostSort sort) {
        return (sort == null) ? "LATEST" : sort.name();
    }

    // ---------- 게시글 ----------
    @Override
    public Long createPost(Long currentMemberId, PostCreateRequest req) {
        List<Long> hashtagIds = req.getHashtagIds() != null ? new ArrayList<>(req.getHashtagIds()) : new ArrayList<>();
        validateCategoryAndTags(req.getCategory(), req.getFreeType(), hashtagIds);
        ensurePostTypeHashtagSynced(req.getCategory(), req.getFreeType(), hashtagIds);

        Post post = new Post();
        post.setMemberId(currentMemberId);
        post.setCategory(req.getCategory());
        post.setFreeType(req.getFreeType());
        post.setTitle(req.getTitle());
        post.setContent(req.getContent());
        post.setThumbnailUrl(req.getThumbnailUrl());
        post.setIsDeleted(false);
        post.setViewCount(0); post.setLikeCount(0); post.setBookmarkCount(0); post.setCommentCount(0);
        communityMapper.insertPost(post);

        if (req.getImageUrls() != null && !req.getImageUrls().isEmpty()) {
            List<PostImage> imgs = new ArrayList<>();
            int order = 1;
            for (String url : req.getImageUrls()) {
                PostImage pi = new PostImage();
                pi.setPostId(post.getId());
                pi.setImageUrl(url);
                pi.setSortOrder(order++);
                imgs.add(pi);
            }
            communityMapper.batchInsertPostImages(post.getId(), imgs);
        }

        if (!hashtagIds.isEmpty()) {
            communityMapper.batchInsertPostHashtags(post.getId(), hashtagIds);
        }

        return post.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> listPosts(PostListRequest req) {
        int size = Optional.ofNullable(req.getSize()).orElse(10);
        int page = Math.max(1, Optional.ofNullable(req.getPage()).orElse(1));
        int offset = (page - 1) * size;

        List<PostListItemResponse> rows = communityMapper.listPosts(
                req.getCategory() == null ? null : req.getCategory().name(),
                req.getFreeType() == null ? null : req.getFreeType().name(),
                req.getHashtagIds(),
                sortKey(req.getSort()),
                size,
                offset
        );
        int total = communityMapper.countPosts(
                req.getCategory() == null ? null : req.getCategory().name(),
                req.getFreeType() == null ? null : req.getFreeType().name(),
                req.getHashtagIds()
        );

        if (!rows.isEmpty()) {
            List<Long> ids = rows.stream().map(PostListItemResponse::getId).collect(Collectors.toList());
            List<PostIdHashtagRow> tagRows = communityMapper.selectHashtagsByPostIdsWithPostId(ids);
            Map<Long, List<HashtagDto>> tagMap = tagRows.stream().collect(Collectors.groupingBy(
                    PostIdHashtagRow::getPostId,
                    Collectors.mapping(r -> new HashtagDto(r.getId(), r.getName(), r.getTagType(), r.getCode()), Collectors.toList())
            ));
            rows.forEach(it -> it.setHashtags(tagMap.getOrDefault(it.getId(), Collections.emptyList())));
        }

        Map<String, Object> res = new HashMap<>();
        res.put("total", total);
        res.put("page", page);
        res.put("size", size);
        res.put("items", rows);
        return res;
    }

    @Override
    @Transactional
    public PostDetailResponse getPostDetail(Long postId) {
        communityMapper.incrementViewCount(postId);
        Post p = communityMapper.selectPostById(postId);
        if (p == null || Boolean.TRUE.equals(p.getIsDeleted())) {
            throw new NoSuchElementException("게시글을 찾을 수 없습니다.");
        }
        PostDetailResponse dto = new PostDetailResponse();
        dto.setId(p.getId());
        dto.setMemberId(p.getMemberId());
        dto.setCategory(p.getCategory());
        dto.setFreeType(p.getFreeType());
        dto.setTitle(p.getTitle());
        dto.setContent(p.getContent());
        dto.setThumbnailUrl(p.getThumbnailUrl());
        dto.setViewCount(p.getViewCount() + 1);
        dto.setLikeCount(p.getLikeCount());
        dto.setBookmarkCount(p.getBookmarkCount());
        dto.setCommentCount(p.getCommentCount());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());
        dto.setImages(communityMapper.selectPostImages(postId));
        dto.setHashtags(communityMapper.selectHashtagsByPostId(postId));

        // 댓글 트리(0/1단계)
        List<Comment> comments = communityMapper.selectCommentsByPostId(postId);
        Map<Long, CommentResponse> all = new LinkedHashMap<>();
        List<CommentResponse> roots = new ArrayList<>();
        for (Comment c : comments) {
            CommentResponse cr = new CommentResponse();
            cr.setId(c.getId());
            cr.setPostId(c.getPostId());
            cr.setMemberId(c.getMemberId());
            cr.setParentCommentId(c.getParentCommentId());
            cr.setContent(c.getContent());
            cr.setDepth(c.getDepth());
            cr.setCreatedAt(c.getCreatedAt());
            cr.setUpdatedAt(c.getUpdatedAt());
            all.put(cr.getId(), cr);
        }
        // 부모-자식 연결
        for (CommentResponse cr : all.values()) {
            if (cr.getParentCommentId() == null) {
                roots.add(cr);
            } else {
                CommentResponse parent = all.get(cr.getParentCommentId());
                if (parent != null) parent.getChildren().add(cr);
                else roots.add(cr); // 부모 삭제 등 예외시 루트로
            }
        }
        dto.setComments(roots);

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> searchPosts(String keyword, PostListRequest req) {
        int size = Optional.ofNullable(req.getSize()).orElse(10);
        int page = Math.max(1, Optional.ofNullable(req.getPage()).orElse(1));
        int offset = (page - 1) * size;
        String like = (keyword == null || keyword.isBlank()) ? null : "%" + keyword.trim() + "%";

        List<PostListItemResponse> rows = communityMapper.searchPosts(
                like,
                req.getCategory() == null ? null : req.getCategory().name(),
                req.getFreeType() == null ? null : req.getFreeType().name(),
                req.getHashtagIds(),
                sortKey(req.getSort()),
                size,
                offset
        );
        int total = communityMapper.countSearchPosts(
                like,
                req.getCategory() == null ? null : req.getCategory().name(),
                req.getFreeType() == null ? null : req.getFreeType().name(),
                req.getHashtagIds()
        );

        if (!rows.isEmpty()) {
            List<Long> ids = rows.stream().map(PostListItemResponse::getId).collect(Collectors.toList());
            List<PostIdHashtagRow> tagRows = communityMapper.selectHashtagsByPostIdsWithPostId(ids);
            Map<Long, List<HashtagDto>> tagMap = tagRows.stream().collect(Collectors.groupingBy(
                    PostIdHashtagRow::getPostId,
                    Collectors.mapping(r -> new HashtagDto(r.getId(), r.getName(), r.getTagType(), r.getCode()), Collectors.toList())
            ));
            rows.forEach(it -> it.setHashtags(tagMap.getOrDefault(it.getId(), Collections.emptyList())));
        }

        Map<String, Object> res = new HashMap<>();
        res.put("total", total);
        res.put("page", page);
        res.put("size", size);
        res.put("items", rows);
        return res;
    }

    @Override
    public void updatePost(Long currentMemberId, PostUpdateRequest req) {
        Long ownerId = communityMapper.selectOwnerId(req.getPostId());
        if (ownerId == null) throw new NoSuchElementException("게시글을 찾을 수 없습니다.");
        if (!ownerId.equals(currentMemberId)) throw new SecurityException("본인 게시글만 수정할 수 있습니다.");

        Post existing = communityMapper.selectPostById(req.getPostId());
        if (existing == null || Boolean.TRUE.equals(existing.getIsDeleted())) {
            throw new NoSuchElementException("게시글을 찾을 수 없습니다.");
        }

        List<Long> hashtagIds = req.getHashtagIds() != null ? new ArrayList<>(req.getHashtagIds()) : new ArrayList<>();
        FreeType effectiveFreeType = (req.getFreeType() != null) ? req.getFreeType() : existing.getFreeType();
        validateCategoryAndTags(existing.getCategory(), effectiveFreeType, hashtagIds);
        ensurePostTypeHashtagSynced(existing.getCategory(), effectiveFreeType, hashtagIds);

        Post upd = new Post();
        upd.setId(req.getPostId());
        upd.setMemberId(currentMemberId);
        upd.setTitle(req.getTitle() != null ? req.getTitle() : existing.getTitle());
        upd.setContent(req.getContent() != null ? req.getContent() : existing.getContent());
        upd.setThumbnailUrl(req.getThumbnailUrl() != null ? req.getThumbnailUrl() : existing.getThumbnailUrl());
        upd.setFreeType(effectiveFreeType);
        communityMapper.updatePost(upd);

        communityMapper.deletePostImages(req.getPostId());
        if (req.getImageUrls() != null && !req.getImageUrls().isEmpty()) {
            List<PostImage> imgs = new ArrayList<>();
            int order = 1;
            for (String url : req.getImageUrls()) {
                PostImage pi = new PostImage();
                pi.setPostId(req.getPostId());
                pi.setImageUrl(url);
                pi.setSortOrder(order++);
                imgs.add(pi);
            }
            communityMapper.batchInsertPostImages(req.getPostId(), imgs);
        }

        communityMapper.deletePostHashtags(req.getPostId());
        if (!hashtagIds.isEmpty()) {
            communityMapper.batchInsertPostHashtags(req.getPostId(), hashtagIds);
        }
    }

    @Override
    public void deletePost(Long currentMemberId, Long postId) {
        int affected = communityMapper.softDeletePost(postId, currentMemberId);
        if (affected == 0) throw new SecurityException("본인 게시글만 삭제할 수 있습니다.");
    }

    // ---------- 댓글 ----------
    @Override
    public Long createComment(Long currentMemberId, CommentCreateRequest req) {
        Post p = communityMapper.selectPostById(req.getPostId());
        if (p == null || Boolean.TRUE.equals(p.getIsDeleted())) {
            throw new NoSuchElementException("게시글을 찾을 수 없습니다.");
        }
        Comment c = new Comment();
        c.setPostId(req.getPostId());
        c.setMemberId(currentMemberId);
        c.setParentCommentId(req.getParentCommentId());
        c.setContent(req.getContent());
        c.setDepth(req.getParentCommentId() == null ? 0 : 1);
        communityMapper.insertComment(c);
        communityMapper.incCommentCount(req.getPostId());
        return c.getId();
    }

    @Override
    public void updateComment(Long currentMemberId, CommentUpdateRequest req) {
        Comment existing = communityMapper.selectCommentById(req.getCommentId());
        if (existing == null || Boolean.TRUE.equals(existing.getIsDeleted())) {
            throw new NoSuchElementException("댓글을 찾을 수 없습니다.");
        }
        if (!existing.getMemberId().equals(currentMemberId)) {
            throw new SecurityException("본인 댓글만 수정할 수 있습니다.");
        }
        existing.setContent(req.getContent());
        communityMapper.updateComment(existing);
    }

    @Override
    public void deleteComment(Long currentMemberId, Long commentId) {
        Comment existing = communityMapper.selectCommentById(commentId);
        if (existing == null || Boolean.TRUE.equals(existing.getIsDeleted())) {
            throw new NoSuchElementException("댓글을 찾을 수 없습니다.");
        }
        if (!existing.getMemberId().equals(currentMemberId)) {
            throw new SecurityException("본인 댓글만 삭제할 수 있습니다.");
        }
        int affected = communityMapper.softDeleteComment(commentId, currentMemberId);
        if (affected > 0) communityMapper.decCommentCount(existing.getPostId());
    }

    // ---------- 토글 ----------
    @Override
    public boolean toggleLike(Long currentMemberId, Long postId) {
        Post p = communityMapper.selectPostById(postId);
        if (p == null || Boolean.TRUE.equals(p.getIsDeleted())) throw new NoSuchElementException("게시글을 찾을 수 없습니다.");

        Integer exists = communityMapper.existsLike(postId, currentMemberId);
        if (exists != null && exists > 0) {
            communityMapper.deleteLike(postId, currentMemberId);
            communityMapper.decLikeCount(postId);
            return false; // unliked
        } else {
            communityMapper.insertLike(postId, currentMemberId);
            communityMapper.incLikeCount(postId);
            return true;  // liked
        }
    }

    @Override
    public boolean toggleBookmark(Long currentMemberId, Long postId) {
        Post p = communityMapper.selectPostById(postId);
        if (p == null || Boolean.TRUE.equals(p.getIsDeleted())) throw new NoSuchElementException("게시글을 찾을 수 없습니다.");

        Integer exists = communityMapper.existsBookmark(postId, currentMemberId);
        if (exists != null && exists > 0) {
            communityMapper.deleteBookmark(postId, currentMemberId);
            communityMapper.decBookmarkCount(postId);
            return false; // unbookmarked
        } else {
            communityMapper.insertBookmark(postId, currentMemberId);
            communityMapper.incBookmarkCount(postId);
            return true;  // bookmarked
        }
    }

    // ---------- 인기 & 추천 ----------
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> popular(int size) {
        List<PostListItemResponse> rows = communityMapper.popularPosts(size);
        if (!rows.isEmpty()) {
            List<Long> ids = rows.stream().map(PostListItemResponse::getId).collect(Collectors.toList());
            List<PostIdHashtagRow> tagRows = communityMapper.selectHashtagsByPostIdsWithPostId(ids);
            Map<Long, List<HashtagDto>> tagMap = tagRows.stream().collect(Collectors.groupingBy(
                    PostIdHashtagRow::getPostId,
                    Collectors.mapping(r -> new HashtagDto(r.getId(), r.getName(), r.getTagType(), r.getCode()), Collectors.toList())
            ));
            rows.forEach(it -> it.setHashtags(tagMap.getOrDefault(it.getId(), Collections.emptyList())));
        }
        return Map.of("items", rows, "size", size);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> recommend(Long currentMemberId, int size) {
        List<PostListItemResponse> rows = communityMapper.recommendPostsByMember(currentMemberId, size);
        if (!rows.isEmpty()) {
            List<Long> ids = rows.stream().map(PostListItemResponse::getId).collect(Collectors.toList());
            List<PostIdHashtagRow> tagRows = communityMapper.selectHashtagsByPostIdsWithPostId(ids);
            Map<Long, List<HashtagDto>> tagMap = tagRows.stream().collect(Collectors.groupingBy(
                    PostIdHashtagRow::getPostId,
                    Collectors.mapping(r -> new HashtagDto(r.getId(), r.getName(), r.getTagType(), r.getCode()), Collectors.toList())
            ));
            rows.forEach(it -> it.setHashtags(tagMap.getOrDefault(it.getId(), Collections.emptyList())));
        }
        return Map.of("items", rows, "size", size);
    }
}
