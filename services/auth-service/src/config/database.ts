import { createPool } from '@lambrk/shared';

export const initializeDatabase = () => {
  return createPool({});
};

