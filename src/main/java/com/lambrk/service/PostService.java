package com.lambrk.service;

import com.lambrk.domain.Post;
import com.lambrk.domain.User;
import com.lambrk.domain.Subreddit;
import com.lambrk.dto.PostCreateRequest;
import com.lambrk.dto.PostResponse;
import com.lambrk.repository.PostRepository;
import com.lambrk.repository.SubredditRepository;
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
import java.util.concurrent.StructuredTaskScope;

@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SubredditRepository subredditRepository;
    private final VoteRepository voteRepository;
    private final KafkaEventService kafkaEventService;

    public PostService(PostRepository postRepository, UserRepository userRepository,
                      SubredditRepository subredditRepository, VoteRepository voteRepository,
                      KafkaEventService kafkaEventService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.subredditRepository = subredditRepository;
        this.voteRepository = voteRepository;
        this.kafkaEventService = kafkaEventService;
    }

    @RateLimiter(name = "postCreation")
    @CircuitBreaker(name = "postService")
    @Retry(name = "postService")
    @Bulkhead(name = "postService")
    @CacheEvict(value = {"posts", "hotPosts", "newPosts"}, allEntries = true)
    @ModerateContent(contentType = "post")
    public PostResponse createPost(PostCreateRequest request, Long authorId) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var userFuture = scope.fork(() -> userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found")));
            
            var subredditFuture = scope.fork(() -> subredditRepository.findById(request.subredditId())
                .orElseThrow(() -> new RuntimeException("Subreddit not found")));

            scope.join();
            scope.throwIfFailed();

            User author = userFuture.get();
            Subreddit subreddit = subredditFuture.get();

            Post post = new Post(
                request.title(),
                request.content(),
                request.url(),
                request.postType(),
                author,
                subreddit
            );

            post = new Post(
                post.id(),
                post.title(),
                post.content(),
                post.url(),
                post.postType(),
                post.thumbnailUrl(),
                request.flairText(),
                request.flairCssClass(),
                request.isSpoiler(),
                post.isStickied(),
                post.isLocked(),
                post.isArchived(),
                request.isOver18(),
                post.score(),
                post.upvoteCount(),
                post.downvoteCount(),
                post.commentCount(),
                post.viewCount(),
                post.awardCount(),
                author,
                subreddit,
                post.comments(),
                post.votes(),
                post.createdAt(),
                post.updatedAt(),
                post.archivedAt()
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
    public PostResponse getPost(Long postId, Long currentUserId) {
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
                    .map(vote -> vote.voteType().name())
                    .orElse(null);
            }
        }

        return PostResponse.from(post, userVote);
    }

    @Cacheable(value = "hotPosts", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<PostResponse> getHotPosts(Pageable pageable, Long currentUserId) {
        Page<Post> posts = postRepository.findHotPosts(pageable);
        return posts.map(post -> {
            String userVote = getUserVote(post, currentUserId);
            return PostResponse.from(post, userVote);
        });
    }

    @Cacheable(value = "newPosts", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<PostResponse> getNewPosts(Pageable pageable, Long currentUserId) {
        Page<Post> posts = postRepository.findNewPosts(pageable);
        return posts.map(post -> {
            String userVote = getUserVote(post, currentUserId);
            return PostResponse.from(post, userVote);
        });
    }

    @Cacheable(value = "topPosts", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<PostResponse> getTopPosts(Pageable pageable, Long currentUserId) {
        Page<Post> posts = postRepository.findTopPosts(pageable);
        return posts.map(post -> {
            String userVote = getUserVote(post, currentUserId);
            return PostResponse.from(post, userVote);
        });
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsBySubreddit(Long subredditId, Pageable pageable, Long currentUserId) {
        Subreddit subreddit = subredditRepository.findById(subredditId)
            .orElseThrow(() -> new RuntimeException("Subreddit not found"));
        
        Page<Post> posts = postRepository.findBySubreddit(subreddit, pageable);
        return posts.map(post -> {
            String userVote = getUserVote(post, currentUserId);
            return PostResponse.from(post, userVote);
        });
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByUser(Long userId, Pageable pageable, Long currentUserId) {
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
    public Page<PostResponse> searchPosts(String query, Pageable pageable, Long currentUserId) {
        Page<Post> posts = postRepository.searchPosts(query, pageable);
        return posts.map(post -> {
            String userVote = getUserVote(post, currentUserId);
            return PostResponse.from(post, userVote);
        });
    }

    @CacheEvict(value = "posts", key = "#postId")
    @CircuitBreaker(name = "postService")
    @Retry(name = "postService")
    public PostResponse updatePost(Long postId, PostCreateRequest request, Long currentUserId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.author().id().equals(currentUserId)) {
            throw new RuntimeException("You can only edit your own posts");
        }

        Post updatedPost = new Post(
            post.id(),
            request.title(),
            request.content(),
            request.url(),
            request.postType(),
            post.thumbnailUrl(),
            request.flairText(),
            request.flairCssClass(),
            request.isSpoiler(),
            post.isStickied(),
            post.isLocked(),
            post.isArchived(),
            request.isOver18(),
            post.score(),
            post.upvoteCount(),
            post.downvoteCount(),
            post.commentCount(),
            post.viewCount(),
            post.awardCount(),
            post.author(),
            post.subreddit(),
            post.comments(),
            post.votes(),
            post.createdAt(),
            Instant.now(),
            post.archivedAt()
        );

        Post savedPost = postRepository.save(updatedPost);
        
        // Send Kafka event
        kafkaEventService.sendPostUpdatedEvent(savedPost);

        return PostResponse.from(savedPost);
    }

    @CacheEvict(value = {"posts", "hotPosts", "newPosts", "topPosts", "searchPosts"}, allEntries = true)
    public void deletePost(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.author().id().equals(currentUserId)) {
            throw new RuntimeException("You can only delete your own posts");
        }

        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getStickiedPosts(Long subredditId, Long currentUserId) {
        List<Post> posts = subredditId != null 
            ? postRepository.findStickiedPostsBySubreddit(subredditId)
            : postRepository.findStickiedPosts();
            
        return posts.stream()
            .map(post -> {
                String userVote = getUserVote(post, currentUserId);
                return PostResponse.from(post, userVote);
            })
            .toList();
    }

    private String getUserVote(Post post, Long currentUserId) {
        if (currentUserId == null) return null;
        
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        if (currentUser == null) return null;
        
        return voteRepository.findByUserAndPost(currentUser, post)
            .map(vote -> vote.voteType().name())
            .orElse(null);
    }
}
