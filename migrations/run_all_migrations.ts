import { readFileSync, readdirSync } from 'fs';
import { join } from 'path';
import { createPool } from '@lambrk/shared';
import { getPool } from '@lambrk/shared';

const runAllMigrations = async () => {
  try {
    console.log('Initializing database connection...');
    createPool({});
    const pool = getPool();

    console.log('Running migrations...\n');
    
    // Get all migration files sorted by name
    const migrationsDir = join(process.cwd(), 'migrations');
    const files = readdirSync(migrationsDir)
      .filter(file => file.endsWith('.sql'))
      .sort();

    console.log(`Found ${files.length} migration file(s):\n`);

    for (const file of files) {
      const migrationPath = join(migrationsDir, file);
      console.log(`Running: ${file}...`);
      const migrationFile = readFileSync(migrationPath, 'utf-8');
      await pool.query(migrationFile);
      console.log(`✓ ${file} completed\n`);
    }

    console.log('All migrations completed successfully!');
    console.log('Database: lambrk');
    console.log('Tables: users, videos, video_qualities');

    await pool.end();
    process.exit(0);
  } catch (error) {
    console.error('Migration failed:', error);
    process.exit(1);
  }
};

runAllMigrations();

