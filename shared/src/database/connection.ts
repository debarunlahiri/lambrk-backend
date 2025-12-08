import { Pool, PoolConfig } from 'pg';

let pool: Pool | null = null;

export const createPool = (config: PoolConfig): Pool => {
  if (pool) {
    return pool;
  }

  pool = new Pool({
    host: config.host || process.env.POSTGRES_HOST || 'localhost',
    port: parseInt(process.env.POSTGRES_PORT || '5432'),
    user: config.user || process.env.POSTGRES_USER || 'lambrk_user',
    password: config.password || process.env.POSTGRES_PASSWORD || 'lambrk_password',
    database: config.database || process.env.POSTGRES_DB || 'lambrk',
    max: 20,
    idleTimeoutMillis: 30000,
    connectionTimeoutMillis: 2000,
  });

  pool.on('error', (err) => {
    console.error('Unexpected error on idle client', err);
    process.exit(-1);
  });

  return pool;
};

export const getPool = (): Pool => {
  if (!pool) {
    throw new Error('Database pool not initialized. Call createPool first.');
  }
  return pool;
};

export const closePool = async (): Promise<void> => {
  if (pool) {
    await pool.end();
    pool = null;
  }
};

