package com.guideon.community.controller;

import com.guideon.common.dto.CommonResponseDTO;
import com.guideon.common.exception.UnauthorizedException;
import com.guideon.community.dto.*;
import com.guideon.community.enums.PostSort;
import com.guideon.community.service.CommunityService;
import com.guideon.security.util.LoginUserProvider;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community")
@Api(tags = "커뮤니티 API", description = "게시글/댓글 CRUD, 좋아요/북마크 토글, 검색/인기/추천")
public class CommunityController {

    private final CommunityService communityService;
    private final LoginUserProvider loginUserProvider; // ✅ 컨텍스트에서 로그인 사용자 정보 획득

    private Long currentMemberIdOrThrow() {
        Long memberId = loginUserProvider.getLoginMemberId();
        if (memberId == null) throw new UnauthorizedException("로그인이 필요합니다.");
        return memberId;
    }

    // -----------------------------
    // 게시글
    // -----------------------------
    @PostMapping("/posts")
    @ApiOperation(value = "게시글 작성", notes = "로그인 사용자가 게시글을 작성합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Cookie", value = "로그인 쿠키 (예: accessToken=eyJ...)",
                    required = true, paramType = "header", dataType = "string")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "작성 성공 (postId 반환)"),
            @ApiResponse(code = 401, message = "인증 실패")
    })
    public CommonResponseDTO<Map<String, Object>> createPost(
            @ApiParam(value = "게시글 생성 요청 바디", required = true)
            @RequestBody PostCreateRequest req
    ) {
        Long memberId = currentMemberIdOrThrow();
        Long postId = communityService.createPost(memberId, req);
        return CommonResponseDTO.ok(Map.of("postId", postId));
    }

    @GetMapping("/posts")
    @ApiOperation(value = "게시글 목록 조회", notes = "카테고리/정렬/페이지네이션으로 게시글 목록을 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "category", value = "CASE | FREE", required = false, paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "freeType", value = "QUESTION | PROMO | TIP | OTHER (category=FREE 일 때)", required = false, paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "page", value = "페이지 번호(1-base)", defaultValue = "1", paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "size", value = "페이지 크기", defaultValue = "10", paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "sort", value = "정렬: LATEST(최신) | POPULAR(인기) | VIEWS | BOOKMARKS | LIKES", defaultValue = "LATEST", paramType = "query", dataType = "string")
    })
    public CommonResponseDTO<Map<String, Object>> listPosts(
            @ApiParam(value = "게시글 목록 조회 요청 파라미터(바인딩)") @ModelAttribute PostListRequest req
    ) {
        if (req.getSort() == null) req.setSort(PostSort.LATEST);
        return CommonResponseDTO.ok(communityService.listPosts(req));
    }

    @GetMapping("/posts/{postId}")
    @ApiOperation(value = "게시글 상세 조회", notes = "게시글 단건 상세를 조회합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "postId", value = "게시글 ID", required = true, paramType = "path", dataType = "long")
    })
    public CommonResponseDTO<PostDetailResponse> getDetail(@PathVariable Long postId) {
        return CommonResponseDTO.ok(communityService.getPostDetail(postId));
    }

    @GetMapping("/posts/search")
    @ApiOperation(value = "게시글 검색", notes = "키워드와 목록 조건으로 검색합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "keyword", value = "검색 키워드", required = false, paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "category", value = "CASE | FREE", required = false, paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "freeType", value = "QUESTION | PROMO | TIP | OTHER", required = false, paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "page", value = "페이지 번호(1-base)", defaultValue = "1", paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "size", value = "페이지 크기", defaultValue = "10", paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "sort", value = "정렬: LATEST | POPULAR | VIEWS | BOOKMARKS | LIKES", defaultValue = "LATEST", paramType = "query", dataType = "string")
    })
    public CommonResponseDTO<Map<String, Object>> search(
            @RequestParam(required = false) String keyword,
            @ModelAttribute PostListRequest req
    ) {
        return CommonResponseDTO.ok(communityService.searchPosts(keyword, req));
    }

    @PutMapping("/posts/{postId}")
    @ApiOperation(value = "게시글 수정", notes = "로그인 사용자가 자신의 게시글을 수정합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "postId", value = "게시글 ID", required = true, paramType = "path", dataType = "long"),
            @ApiImplicitParam(name = "Cookie", value = "로그인 쿠키 (예: ACCESS_TOKEN=eyJ...)", required = true, paramType = "header", dataType = "string")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "수정 성공"),
            @ApiResponse(code = 401, message = "인증 실패"),
            @ApiResponse(code = 403, message = "권한 없음(작성자 아님)")
    })
    public CommonResponseDTO<Map<String, Object>> update(
            @PathVariable Long postId,
            @ApiParam(value = "게시글 수정 요청 바디", required = true)
            @RequestBody PostUpdateRequest req
    ) {
        Long memberId = currentMemberIdOrThrow();
        req.setPostId(postId);
        communityService.updatePost(memberId, req);
        return CommonResponseDTO.ok(Map.of("postId", postId, "updated", true));
    }

    @DeleteMapping("/posts/{postId}")
    @ApiOperation(value = "게시글 삭제", notes = "로그인 사용자가 자신의 게시글을 삭제합니다(소프트 삭제 가능).")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "postId", value = "게시글 ID", required = true, paramType = "path", dataType = "long"),
            @ApiImplicitParam(name = "Cookie", value = "로그인 쿠키 (예: ACCESS_TOKEN=eyJ...)", required = true, paramType = "header", dataType = "string")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "삭제 성공"),
            @ApiResponse(code = 401, message = "인증 실패"),
            @ApiResponse(code = 403, message = "권한 없음(작성자 아님)")
    })
    public CommonResponseDTO<Map<String, Object>> delete(@PathVariable Long postId) {
        Long memberId = currentMemberIdOrThrow();
        communityService.deletePost(memberId, postId);
        return CommonResponseDTO.ok(Map.of("postId", postId, "deleted", true));
    }

    // -----------------------------
    // 댓글
    // -----------------------------
    @PostMapping("/posts/{postId}/comments")
    @ApiOperation(value = "댓글 작성", notes = "로그인 사용자가 댓글을 작성합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "postId", value = "게시글 ID", required = true, paramType = "path", dataType = "long"),
            @ApiImplicitParam(name = "Cookie", value = "로그인 쿠키 (예: ACCESS_TOKEN=eyJ...)", required = true, paramType = "header", dataType = "string")
    })
    public CommonResponseDTO<Map<String, Object>> createComment(
            @PathVariable Long postId,
            @ApiParam(value = "댓글 생성 요청 바디", required = true)
            @RequestBody CommentCreateRequest req
    ) {
        Long memberId = currentMemberIdOrThrow();
        req.setPostId(postId);
        Long id = communityService.createComment(memberId, req);
        return CommonResponseDTO.ok(Map.of("commentId", id));
    }

    @PutMapping("/comments/{commentId}")
    @ApiOperation(value = "댓글 수정", notes = "로그인 사용자가 자신의 댓글을 수정합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "commentId", value = "댓글 ID", required = true, paramType = "path", dataType = "long"),
            @ApiImplicitParam(name = "Cookie", value = "로그인 쿠키 (예: ACCESS_TOKEN=eyJ...)", required = true, paramType = "header", dataType = "string")
    })
    public CommonResponseDTO<Map<String, Object>> updateComment(
            @PathVariable Long commentId,
            @ApiParam(value = "댓글 수정 요청 바디", required = true)
            @RequestBody CommentUpdateRequest req
    ) {
        Long memberId = currentMemberIdOrThrow();
        req.setCommentId(commentId);
        communityService.updateComment(memberId, req);
        return CommonResponseDTO.ok(Map.of("commentId", commentId, "updated", true));
    }

    @DeleteMapping("/comments/{commentId}")
    @ApiOperation(value = "댓글 삭제", notes = "로그인 사용자가 자신의 댓글을 삭제합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "commentId", value = "댓글 ID", required = true, paramType = "path", dataType = "long"),
            @ApiImplicitParam(name = "Cookie", value = "로그인 쿠키 (예: ACCESS_TOKEN=eyJ...)", required = true, paramType = "header", dataType = "string")
    })
    public CommonResponseDTO<Map<String, Object>> deleteComment(@PathVariable Long commentId) {
        Long memberId = currentMemberIdOrThrow();
        communityService.deleteComment(memberId, commentId);
        return CommonResponseDTO.ok(Map.of("commentId", commentId, "deleted", true));
    }

    // -----------------------------
    // 토글 (좋아요/북마크)
    // -----------------------------
    @PostMapping("/posts/{postId}/like/toggle")
    @ApiOperation(value = "좋아요 토글", notes = "로그인 사용자가 좋아요/좋아요취소를 토글합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "postId", value = "게시글 ID", required = true, paramType = "path", dataType = "long"),
            @ApiImplicitParam(name = "Cookie", value = "로그인 쿠키 (예: ACCESS_TOKEN=eyJ...)", required = true, paramType = "header", dataType = "string")
    })
    public CommonResponseDTO<Map<String, Object>> toggleLike(@PathVariable Long postId) {
        Long memberId = currentMemberIdOrThrow();
        boolean liked = communityService.toggleLike(memberId, postId);
        return CommonResponseDTO.ok(Map.of("postId", postId, "liked", liked));
    }

    @PostMapping("/posts/{postId}/bookmark/toggle")
    @ApiOperation(value = "북마크 토글", notes = "로그인 사용자가 북마크/해제를 토글합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "postId", value = "게시글 ID", required = true, paramType = "path", dataType = "long"),
            @ApiImplicitParam(name = "Cookie", value = "로그인 쿠키 (예: ACCESS_TOKEN=eyJ...)", required = true, paramType = "header", dataType = "string")
    })
    public CommonResponseDTO<Map<String, Object>> toggleBookmark(@PathVariable Long postId) {
        Long memberId = currentMemberIdOrThrow();
        boolean bookmarked = communityService.toggleBookmark(memberId, postId);
        return CommonResponseDTO.ok(Map.of("postId", postId, "bookmarked", bookmarked));
    }

    // -----------------------------
    // 인기/추천
    // -----------------------------
    @GetMapping("/posts/popular")
    @ApiOperation(value = "인기글 조회", notes = "조회/좋아요/북마크 등을 기준으로 인기 글을 가져옵니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "size", value = "목록 크기", defaultValue = "10", paramType = "query", dataType = "int")
    })
    public CommonResponseDTO<Map<String, Object>> popular(@RequestParam(defaultValue = "10") int size) {
        return CommonResponseDTO.ok(communityService.popular(size));
    }

    @GetMapping("/posts/recommend")
    @ApiOperation(value = "추천글 조회", notes = "로그인 사용자의 관심/행동 기반 추천 글을 가져옵니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "size", value = "목록 크기", defaultValue = "10", paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "Cookie", value = "로그인 쿠키 (예: ACCESS_TOKEN=eyJ...)", required = true, paramType = "header", dataType = "string")
    })
    public CommonResponseDTO<Map<String, Object>> recommend(@RequestParam(defaultValue = "10") int size) {
        Long memberId = currentMemberIdOrThrow();
        return CommonResponseDTO.ok(communityService.recommend(memberId, size));
    }
}
