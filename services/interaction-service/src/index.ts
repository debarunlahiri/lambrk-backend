import express, { Application } from 'express';
import cors from 'cors';
import { initializeDatabase } from './config/database';
import { errorHandler } from '@lambrk/shared';
import interactionRoutes from './routes/interactionRoutes';

const app: Application = express();
const PORT = process.env.PORT || 4405;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Initialize database
initializeDatabase();

// Routes
app.use('/api', interactionRoutes);

// Health check
app.get('/health', (req, res) => {
  res.status(200).json({ status: 'ok', service: 'interaction-service' });
});

// Error handler (should be last)
app.use(errorHandler);

// Start server
app.listen(PORT, () => {
  console.log(`Interaction Service running on port ${PORT}`);
});

export default app;
