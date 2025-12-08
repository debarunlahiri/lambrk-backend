import { readFileSync } from 'fs';
import { join } from 'path';
import { getPool } from '@lambrk/shared';
import { initializeDatabase } from '../config/database';

const runMigrations = async () => {
  try {
    initializeDatabase();
    const pool = getPool();

    const migrationFile = readFileSync(
      join(__dirname, '001_create_users_table.sql'),
      'utf-8'
    );

    await pool.query(migrationFile);
    console.log('Migration completed successfully');

    await pool.end();
    process.exit(0);
  } catch (error) {
    console.error('Migration failed:', error);
    process.exit(1);
  }
};

runMigrations();

