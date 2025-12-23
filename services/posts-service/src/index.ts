import express, { Application } from 'express';
import cors from 'cors';
import { initializeDatabase } from './config/database';
import { errorHandler } from '@lambrk/shared';
import postRoutes from './routes/postRoutes';

const app: Application = express();
const PORT = process.env.PORT || 3104;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Initialize database
initializeDatabase();

// Routes
app.use('/api/posts', postRoutes);

// Health check
app.get('/health', (req, res) => {
  res.status(200).json({ status: 'ok', service: 'posts-service' });
});

// Error handler (should be last)
app.use(errorHandler);

// Start server
app.listen(PORT, () => {
  console.log(`Posts Service running on port ${PORT}`);
});

export default app;
