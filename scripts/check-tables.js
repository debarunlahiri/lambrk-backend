const { Pool } = require('pg');

// Try to load .env file if it exists
require('dotenv').config();

const DB_HOST = process.env.POSTGRES_HOST || 'localhost';
const DB_PORT = parseInt(process.env.POSTGRES_PORT || '5432');
const DB_USER = process.env.POSTGRES_USER || process.env.USER || 'lambrk_user';
const DB_PASSWORD = process.env.POSTGRES_PASSWORD || '';
const DB_NAME = process.env.POSTGRES_DB || 'lambrk';

console.log('Database Configuration:');
console.log(`  Host: ${DB_HOST}`);
console.log(`  Port: ${DB_PORT}`);
console.log(`  User: ${DB_USER}`);
console.log(`  Database: ${DB_NAME}`);
console.log('');

const pool = new Pool({
  host: DB_HOST,
  port: DB_PORT,
  user: DB_USER,
  password: DB_PASSWORD,
  database: DB_NAME,
});

async function checkTables() {
  try {
    console.log(`Connecting to database: ${DB_NAME} on ${DB_HOST}:${DB_PORT}`);
    console.log(`User: ${DB_USER}\n`);

    // Check if database exists
    const dbCheck = await pool.query(
      "SELECT 1 FROM pg_database WHERE datname = $1",
      [DB_NAME]
    );

    if (dbCheck.rows.length === 0) {
      console.log(`❌ Database '${DB_NAME}' does not exist!`);
      process.exit(1);
    }

    console.log(`✓ Database '${DB_NAME}' exists\n`);

    // List all tables
    const tablesResult = await pool.query(`
      SELECT 
        table_name,
        table_schema
      FROM information_schema.tables
      WHERE table_schema = 'public'
      ORDER BY table_name;
    `);

    if (tablesResult.rows.length === 0) {
      console.log('❌ No tables found in the database!');
      console.log('\nRunning migrations...\n');
      process.exit(1);
    }

    console.log(`Found ${tablesResult.rows.length} table(s):\n`);
    tablesResult.rows.forEach((row, index) => {
      console.log(`${index + 1}. ${row.table_name}`);
    });

    // Check for required tables
    const requiredTables = ['users', 'videos'];
    const existingTables = tablesResult.rows.map(row => row.table_name);
    const missingTables = requiredTables.filter(table => !existingTables.includes(table));

    if (missingTables.length > 0) {
      console.log(`\n⚠️  Missing required tables: ${missingTables.join(', ')}`);
    } else {
      console.log('\n✓ All required tables exist!');
    }

    // Show table details
    console.log('\n--- Table Details ---\n');
    for (const table of requiredTables) {
      if (existingTables.includes(table)) {
        const columnsResult = await pool.query(`
          SELECT 
            column_name,
            data_type,
            is_nullable,
            column_default
          FROM information_schema.columns
          WHERE table_schema = 'public' AND table_name = $1
          ORDER BY ordinal_position;
        `, [table]);

        console.log(`Table: ${table}`);
        console.log(`Columns (${columnsResult.rows.length}):`);
        columnsResult.rows.forEach(col => {
          const nullable = col.is_nullable === 'YES' ? 'NULL' : 'NOT NULL';
          const defaultVal = col.column_default ? ` DEFAULT ${col.column_default}` : '';
          console.log(`  - ${col.column_name}: ${col.data_type} ${nullable}${defaultVal}`);
        });

        // Get row count
        const countResult = await pool.query(`SELECT COUNT(*) as count FROM ${table}`);
        console.log(`  Rows: ${countResult.rows[0].count}\n`);
      }
    }

    await pool.end();
    process.exit(0);
  } catch (error) {
    console.error('Error:', error.message);
    await pool.end();
    process.exit(1);
  }
}

checkTables();

