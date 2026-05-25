package com.lambrk.service;

import com.lambrk.domain.Post;
import com.lambrk.domain.User;
import com.lambrk.domain.Community;
import com.lambrk.dto.PostCreateRequest;
import com.lambrk.dto.PostResponse;
import com.lambrk.repository.PostRepository;
import com.lambrk.repository.CommunityRepository;
import com.lambrk.repository.UserRepository;
import com.lambrk.repository.VoteRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final VoteRepository voteRepository;
    private final KafkaEventService kafkaEventService;

    public PostService(PostRepository postRepository, UserRepository userRepository,
                      CommunityRepository communityRepository, VoteRepository voteRepository,
                      KafkaEventService kafkaEventService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.communityRepository = communityRepository;
        this.voteRepository = voteRepository;
        this.kafkaEventService = kafkaEventService;
    }

    @RateLimiter(name = "postCreation")
    @CircuitBreaker(name = "postService")
    @Retry(name = "postService")
    @Bulkhead(name = "postService")
    @CacheEvict(value = {"posts", "hotPosts", "newPosts"}, allEntries = true)
    @ModerateContent(contentType = "post")
    public PostResponse createPost(PostCreateRequest request, UUID authorId) {
        try {
            User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            Community community = request.communityId() != null
                ? communityRepository.findById(request.communityId()).orElse(null)
                : null;

            Post post = new Post(
                request.title(),
                request.content(),
                request.url(),
                request.postType(),
                author,
                community
            );

            post = new Post(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getUrl(),
                post.getPostType(),
                post.getThumbnailUrl(),
                request.flairText(),
                request.flairCssClass(),
                request.isSpoiler(),
                post.isStickied(),
                post.isLocked(),
                post.isArchived(),
                post.isRemoved(),
                request.isOver18(),
            post.getScore(),
            post.getLikeCount(),
            post.getDislikeCount(),
                post.getCommentCount(),
                post.getViewCount(),
                post.getAwardCount(),
                author,
                community,
                post.getComments(),
                post.getVotes(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getArchivedAt()
            );

            Post savedPost = postRepository.save(post);
            
            // Update user karma
            userRepository.updateUserKarma(authorId, 1);
            
            // Send Kafka event
            kafkaEventService.sendPostCreatedEvent(savedPost);

            return PostResponse.from(savedPost);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create post", e);
        }
    }

    @Cacheable(value = "posts", key = "#postId")
    @Transactional(readOnly = true)
    public PostResponse getPost(UUID postId, UUID currentUserId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));

        // Increment view count asynchronously
        if (currentUserId != null) {
            postRepository.incrementPostViewCount(postId);
        }

        String userVote = null;
        if (currentUserId != null) {
            User currentUser = userRepository.findById(currentUserId).orElse(null);
            if (currentUser != null) {
                userVote = voteRepository.findByUserAndPost(currentUser, post)
                    .map(vote -> vote.getVoteType().name())
                    .orElse(null);
            }
        }

        return PostResponse.from(post, userVote);
    }

    @Cacheable(value = "hotPosts", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<PostResponse> getHotPosts(Pageable pageable, UUID currentUserId) {
        Page<Post> posts = postRepository.findHotPosts(pageable);
        return posts.map(post -> {
            String userVote = getUserVote(post, currentUserId);
            return PostResponse.from(post, userVote);
        });
    }

    @Cacheable(value = "newPosts", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<PostResponse> getNewPosts(Pageable pageable, UUID currentUserId) {
        Page<Post> posts = postRepository.findNewPosts(pageable);
        return posts.map(post -> {
            String userVote = getUserVote(post, currentUserId);
            return PostResponse.from(post, userVote);
        });
    }

    @Cacheable(value = "topPosts", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<PostResponse> getTopPosts(Pageable pageable, UUID currentUserId) {
        Page<Post> posts = postRepository.findTopPosts(pageable);
        return posts.map(post -> {
            String userVote = getUserVote(post, currentUserId);
            return PostResponse.from(post, userVote);
        });
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByCommunity(UUID communityId, Pageable pageable, UUID currentUserId) {
        Community community = communityRepository.findById(communityId)
            .orElseThrow(() -> new RuntimeException("Community not found"));
        
        Page<Post> posts = postRepository.findByCommunity(community, pageable);
        return posts.map(post -> {
            String userVote = getUserVote(post, currentUserId);
            return PostResponse.from(post, userVote);
        });
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByUser(UUID userId, Pageable pageable, UUID currentUserId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Page<Post> posts = postRepository.findByAuthor(user, pageable);
        return posts.map(post -> {
            String userVote = getUserVote(post, currentUserId);
            return PostResponse.from(post, userVote);
        });
    }

    @Cacheable(value = "searchPosts", key = "#query + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<PostResponse> searchPosts(String query, Pageable pageable, UUID currentUserId) {
        Page<Post> posts = postRepository.searchPosts(query, pageable);
        return posts.map(post -> {
            String userVote = getUserVote(post, currentUserId);
            return PostResponse.from(post, userVote);
        });
    }

    @CacheEvict(value = "posts", key = "#postId")
    @CircuitBreaker(name = "postService")
    @Retry(name = "postService")
    public PostResponse updatePost(UUID postId, PostCreateRequest request, UUID currentUserId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getAuthor().getId().equals(currentUserId)) {
            throw new RuntimeException("You can only edit your own posts");
        }

        Post updatedPost = new Post(
            post.getId(),
            request.title(),
            request.content(),
            request.url(),
            request.postType(),
            post.getThumbnailUrl(),
            request.flairText(),
            request.flairCssClass(),
            request.isSpoiler(),
            post.isStickied(),
            post.isLocked(),
            post.isArchived(),
            post.isRemoved(),
            request.isOver18(),
            post.getScore(),
                post.getLikeCount(),
                post.getDislikeCount(),
            post.getCommentCount(),
            post.getViewCount(),
            post.getAwardCount(),
            post.getAuthor(),
            post.getCommunity(),
            post.getComments(),
            post.getVotes(),
            post.getCreatedAt(),
            Instant.now(),
            post.getArchivedAt()
        );

        Post savedPost = postRepository.save(updatedPost);
        
        // Send Kafka event
        kafkaEventService.sendPostUpdatedEvent(savedPost);

        return PostResponse.from(savedPost);
    }

    @CacheEvict(value = {"posts", "hotPosts", "newPosts", "topPosts", "searchPosts"}, allEntries = true)
    public void deletePost(UUID postId, UUID currentUserId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getAuthor().getId().equals(currentUserId)) {
            throw new RuntimeException("You can only delete your own posts");
        }

        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getStickiedPosts(UUID communityId, UUID currentUserId) {
        List<Post> posts = communityId != null 
            ? postRepository.findStickiedPostsByCommunity(communityId)
            : postRepository.findStickiedPosts();
            
        return posts.stream()
            .map(post -> {
                String userVote = getUserVote(post, currentUserId);
                return PostResponse.from(post, userVote);
            })
            .toList();
    }

    private String getUserVote(Post post, UUID currentUserId) {
        if (currentUserId == null) return null;
        
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        if (currentUser == null) return null;
        
        return voteRepository.findByUserAndPost(currentUser, post)
            .map(vote -> vote.getVoteType().name())
            .orElse(null);
    }
}
