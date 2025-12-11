import { Router } from 'express';
import { getHealth } from '../controllers/gatewayController';

const router = Router();

router.get('/health', getHealth);

export default router;

