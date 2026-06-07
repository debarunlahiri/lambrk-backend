package com.lambrk.service;

import com.lambrk.domain.Bookmark;
import com.lambrk.domain.Post;
import com.lambrk.domain.User;
import com.lambrk.repository.BookmarkRepository;
import com.lambrk.repository.PostRepository;
import com.lambrk.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public BookmarkService(BookmarkRepository bookmarkRepository,
                          UserRepository userRepository,
                          PostRepository postRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    public void bookmarkPost(UUID userId, UUID postId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));

        if (bookmarkRepository.existsByUserAndPost(user, post)) {
            return; // Already bookmarked, idempotent
        }

        Bookmark bookmark = new Bookmark(java.util.UUID.randomUUID(), user, post);
        bookmarkRepository.save(bookmark);
    }

    public void unbookmarkPost(UUID userId, UUID postId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found"));

        bookmarkRepository.findByUserAndPost(user, post)
            .ifPresent(bookmarkRepository::delete);
    }

    @Transactional(readOnly = true)
    public boolean isBookmarked(UUID userId, UUID postId) {
        User user = userRepository.findById(userId).orElse(null);
        Post post = postRepository.findById(postId).orElse(null);
        if (user == null || post == null) {
            return false;
        }
        return bookmarkRepository.existsByUserAndPost(user, post);
    }

    @Transactional(readOnly = true)
    public Page<Post> getBookmarkedPosts(UUID userId, int page, int size) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Bookmark> bookmarks = bookmarkRepository.findByUser(user, pageable);

        // Force initialization of lazy-loaded post.media while session is open
        bookmarks.getContent().forEach(b -> {
            Post post = b.getPost();
            if (post != null) {
                post.getMedia().size();
                post.getAuthor().getUsername();
                if (post.getCommunity() != null) {
                    post.getCommunity().getName();
                }
            }
        });

        return bookmarks.map(Bookmark::getPost);
    }

    @Transactional(readOnly = true)
    public Set<UUID> getBookmarkedPostIds(UUID userId) {
        return bookmarkRepository.findBookmarkedPostIdsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public long getBookmarkCount(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return 0;
        }
        return bookmarkRepository.countByUser(user);
    }
}
