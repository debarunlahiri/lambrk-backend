import { Router } from 'express';
import { authenticate, createGeneralRateLimiter } from '@lambrk/shared';

// Like Controller
import {
  toggleLike,
  getLikeStats,
  getUserLikedContent,
  toggleLikeValidation,
} from '../controllers/likeController';

// Dislike Controller
import {
  toggleDislike,
  getUserDislikedContent,
  toggleDislikeValidation,
} from '../controllers/dislikeController';

// Report Controller
import {
  createReport,
  getReport,
  updateReport,
  getContentReports,
  getUserReports,
  getReportsByStatus,
  getReportCount,
  checkUserReported,
  createReportValidation,
  updateReportValidation,
} from '../controllers/reportController';

// Share Controller
import {
  createShare,
  getShare,
  getContentShares,
  getUserShares,
  getShareCount,
  getSharesByPlatform,
  createShareValidation,
} from '../controllers/shareController';

// Comment Controller
import {
  createComment,
  getComment,
  getContentComments,
  getCommentReplies,
  updateComment,
  deleteComment,
  getCommentCount,
  createCommentValidation,
  updateCommentValidation,
} from '../controllers/commentController';

// Playlist Controller
import {
  createPlaylist,
  getPlaylist,
  getUserPlaylists,
  getWatchLater,
  updatePlaylist,
  deletePlaylist,
  addToPlaylist,
  removeFromPlaylist,
  getPlaylistItems,
  checkItemInPlaylist,
  createPlaylistValidation,
  updatePlaylistValidation,
  addToPlaylistValidation,
} from '../controllers/playlistController';

// Subscription Controller
import {
  subscribe,
  unsubscribe,
  checkSubscription,
  getUserSubscriptions,
  getChannelSubscribers,
  getSubscriberCount,
  subscribeValidation,
} from '../controllers/subscriptionController';

// Download Controller
import {
  createDownload,
  getDownload,
  getUserDownloads,
  updateDownload,
  deleteDownload,
  createDownloadValidation,
  updateDownloadValidation,
} from '../controllers/downloadController';

// Trending Controller
import {
  getTrendingVideos,
  getTrendingBitz,
  getTrendingPosts,
  refreshTrending,
} from '../controllers/trendingController';

// Recommendation Controller
import {
  getRecommendedVideos,
  getRecommendedPosts,
  getRecommendedBitz,
  getRecommendedTrending,
} from '../controllers/recommendationController';

const router = Router();

// Rate limiters
const readLimiter = createGeneralRateLimiter({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // 100 requests per 15 minutes
});

const writeLimiter = createGeneralRateLimiter({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 50, // 50 requests per 15 minutes
});

// ============================================
// LIKE ROUTES
// ============================================
router.post('/likes', writeLimiter, authenticate as any, toggleLikeValidation, toggleLike as any);
router.get('/likes/:contentType/:contentId', readLimiter, getLikeStats);
router.get('/likes/user/:contentType', readLimiter, authenticate as any, getUserLikedContent as any);

// ============================================
// DISLIKE ROUTES
// ============================================
router.post('/dislikes', writeLimiter, authenticate as any, toggleDislikeValidation, toggleDislike as any);
router.get('/dislikes/user/:contentType', readLimiter, authenticate as any, getUserDislikedContent as any);

// ============================================
// REPORT ROUTES
// ============================================
router.post('/reports', writeLimiter, authenticate as any, createReportValidation, createReport as any);
router.get('/reports/:id', readLimiter, getReport);
router.put('/reports/:id', writeLimiter, authenticate as any, updateReportValidation, updateReport as any);
router.get('/reports/:contentType/:contentId', readLimiter, getContentReports);
router.get('/reports/user/my-reports', readLimiter, authenticate as any, getUserReports as any);
router.get('/reports/status/:status', readLimiter, getReportsByStatus);
router.get('/reports/:contentType/:contentId/count', readLimiter, getReportCount);
router.get('/reports/:contentType/:contentId/check', readLimiter, authenticate as any, checkUserReported as any);

// ============================================
// SHARE ROUTES
// ============================================
router.post('/shares', writeLimiter, authenticate as any, createShareValidation, createShare as any);
router.get('/shares/:id', readLimiter, getShare);
router.get('/shares/:contentType/:contentId', readLimiter, getContentShares);
router.get('/shares/user/my-shares', readLimiter, authenticate as any, getUserShares as any);
router.get('/shares/:contentType/:contentId/count', readLimiter, getShareCount);
router.get('/shares/platform/:platform', readLimiter, getSharesByPlatform);

// ============================================
// COMMENT ROUTES
// ============================================
router.post('/comments', writeLimiter, authenticate as any, createCommentValidation, createComment as any);
router.get('/comments/:id', readLimiter, getComment);
router.get('/comments/:contentType/:contentId', readLimiter, getContentComments);
router.get('/comments/:commentId/replies', readLimiter, getCommentReplies);
router.put('/comments/:id', writeLimiter, authenticate as any, updateCommentValidation, updateComment as any);
router.delete('/comments/:id', writeLimiter, authenticate as any, deleteComment as any);
router.get('/comments/:contentType/:contentId/count', readLimiter, getCommentCount);

// ============================================
// PLAYLIST ROUTES
// ============================================
router.post('/playlists', writeLimiter, authenticate as any, createPlaylistValidation, createPlaylist as any);
router.get('/playlists/watch-later', readLimiter, authenticate as any, getWatchLater as any);
router.get('/playlists/my-playlists', readLimiter, authenticate as any, getUserPlaylists as any);
router.get('/playlists/:id', readLimiter, getPlaylist);
router.put('/playlists/:id', writeLimiter, authenticate as any, updatePlaylistValidation, updatePlaylist as any);
router.delete('/playlists/:id', writeLimiter, authenticate as any, deletePlaylist as any);
router.post('/playlists/:id/items', writeLimiter, authenticate as any, addToPlaylistValidation, addToPlaylist as any);
router.delete('/playlists/:id/items/:contentType/:contentId', writeLimiter, authenticate as any, removeFromPlaylist as any);
router.get('/playlists/:id/items', readLimiter, getPlaylistItems);
router.get('/playlists/:id/items/:contentType/:contentId/check', readLimiter, checkItemInPlaylist);

// ============================================
// SUBSCRIPTION ROUTES
// ============================================
router.post('/subscriptions', writeLimiter, authenticate as any, subscribeValidation, subscribe as any);
router.delete('/subscriptions/:channelId', writeLimiter, authenticate as any, unsubscribe as any);
router.get('/subscriptions/check/:channelId', readLimiter, authenticate as any, checkSubscription as any);
router.get('/subscriptions/my-subscriptions', readLimiter, authenticate as any, getUserSubscriptions as any);
router.get('/subscriptions/channel/:channelId/subscribers', readLimiter, getChannelSubscribers);
router.get('/subscriptions/channel/:channelId/count', readLimiter, getSubscriberCount);

// ============================================
// DOWNLOAD ROUTES
// ============================================
router.post('/downloads', writeLimiter, authenticate as any, createDownloadValidation, createDownload as any);
router.get('/downloads/my-downloads', readLimiter, authenticate as any, getUserDownloads as any);
router.get('/downloads/:id', readLimiter, getDownload);
router.put('/downloads/:id', writeLimiter, authenticate as any, updateDownloadValidation, updateDownload as any);
router.delete('/downloads/:id', writeLimiter, authenticate as any, deleteDownload as any);

// ============================================
// TRENDING ROUTES
// ============================================
router.get('/trending/videos', readLimiter, getTrendingVideos);
router.get('/trending/bitz', readLimiter, getTrendingBitz);
router.get('/trending/posts', readLimiter, getTrendingPosts);
router.post('/trending/refresh', writeLimiter, refreshTrending);

// ============================================
// RECOMMENDATION ROUTES
// ============================================
router.get('/recommendations/videos', readLimiter, authenticate as any, getRecommendedVideos as any);
router.get('/recommendations/posts', readLimiter, authenticate as any, getRecommendedPosts as any);
router.get('/recommendations/bitz', readLimiter, authenticate as any, getRecommendedBitz as any);
router.get('/recommendations/trending/:contentType', readLimiter, getRecommendedTrending);

export default router;
