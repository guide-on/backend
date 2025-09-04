package com.guideon.community.mapper;

import com.guideon.community.domain.Hashtag;
import com.guideon.community.domain.Post;
import com.guideon.community.domain.PostImage;
import com.guideon.community.domain.Comment;
import com.guideon.community.dto.HashtagDto;
import com.guideon.community.dto.PostIdHashtagRow;
import com.guideon.community.dto.PostListItemResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommunityMapper {

    // Post CRUD
    int insertPost(Post post);
    int updatePost(Post post);
    int softDeletePost(@Param("postId") Long postId, @Param("memberId") Long memberId);
    Post selectPostById(@Param("postId") Long postId);
    int incrementViewCount(@Param("postId") Long postId);
    Long selectOwnerId(@Param("postId") Long postId);

    // Images
    int deletePostImages(@Param("postId") Long postId);
    int batchInsertPostImages(@Param("postId") Long postId, @Param("images") List<PostImage> images);
    List<String> selectPostImages(@Param("postId") Long postId);

    // Hashtags
    int deletePostHashtags(@Param("postId") Long postId);
    int batchInsertPostHashtags(@Param("postId") Long postId, @Param("hashtagIds") List<Long> hashtagIds);
    List<HashtagDto> selectHashtagsByPostId(@Param("postId") Long postId);
    List<Hashtag> selectHashtagsByIds(@Param("ids") List<Long> ids);
    Hashtag selectPostTypeHashtagByCode(@Param("code") String code);
    List<PostIdHashtagRow> selectHashtagsByPostIdsWithPostId(@Param("postIds") List<Long> postIds);

    // List/Search
    List<PostListItemResponse> listPosts(
            @Param("category") String category,
            @Param("freeType") String freeType,
            @Param("hashtagIds") List<Long> hashtagIds,
            @Param("sort") String sort,
            @Param("limit") int limit,
            @Param("offset") int offset
    );
    int countPosts(
            @Param("category") String category,
            @Param("freeType") String freeType,
            @Param("hashtagIds") List<Long> hashtagIds
    );

    List<PostListItemResponse> searchPosts(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("freeType") String freeType,
            @Param("hashtagIds") List<Long> hashtagIds,
            @Param("sort") String sort,
            @Param("limit") int limit,
            @Param("offset") int offset
    );
    int countSearchPosts(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("freeType") String freeType,
            @Param("hashtagIds") List<Long> hashtagIds
    );

    // Like & Bookmark toggle helpers
    Integer existsLike(@Param("postId") Long postId, @Param("memberId") Long memberId);
    int insertLike(@Param("postId") Long postId, @Param("memberId") Long memberId);
    int deleteLike(@Param("postId") Long postId, @Param("memberId") Long memberId);
    int incLikeCount(@Param("postId") Long postId);
    int decLikeCount(@Param("postId") Long postId);

    Integer existsBookmark(@Param("postId") Long postId, @Param("memberId") Long memberId);
    int insertBookmark(@Param("postId") Long postId, @Param("memberId") Long memberId);
    int deleteBookmark(@Param("postId") Long postId, @Param("memberId") Long memberId);
    int incBookmarkCount(@Param("postId") Long postId);
    int decBookmarkCount(@Param("postId") Long postId);

    // Comments
    int insertComment(Comment comment);
    int updateComment(Comment comment);
    int softDeleteComment(@Param("commentId") Long commentId, @Param("memberId") Long memberId);
    Comment selectCommentById(@Param("commentId") Long commentId);
    List<Comment> selectCommentsByPostId(@Param("postId") Long postId);
    int incCommentCount(@Param("postId") Long postId);
    int decCommentCount(@Param("postId") Long postId);

    // Popular & Recommend
    List<PostListItemResponse> popularPosts(@Param("limit") int limit);
    List<PostListItemResponse> recommendPostsByMember(@Param("memberId") Long memberId,
                                                      @Param("limit") int limit);
}
