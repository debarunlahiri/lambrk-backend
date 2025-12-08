const { Pool } = require('pg');

const DB_HOST = process.env.POSTGRES_HOST || 'localhost';
const DB_PORT = parseInt(process.env.POSTGRES_PORT || '5432');
const DB_USER = process.env.POSTGRES_USER || process.env.USER || 'postgres';
const DB_PASSWORD = process.env.POSTGRES_PASSWORD || '';

// Connect to postgres database to list all databases
const adminPool = new Pool({
  host: DB_HOST,
  port: DB_PORT,
  user: DB_USER,
  password: DB_PASSWORD,
  database: 'postgres',
});

async function listDatabases() {
  try {
    console.log(`Connecting to PostgreSQL on ${DB_HOST}:${DB_PORT}`);
    console.log(`User: ${DB_USER}\n`);

    const result = await pool.query(`
      SELECT 
        datname as database_name,
        pg_size_pretty(pg_database_size(datname)) as size
      FROM pg_database
      WHERE datistemplate = false
      ORDER BY datname;
    `);

    console.log('Available databases:');
    console.log('-------------------');
    result.rows.forEach((row, index) => {
      console.log(`${index + 1}. ${row.database_name} (${row.size})`);
    });

    await pool.end();
    process.exit(0);
  } catch (error) {
    console.error('Error:', error.message);
    await pool.end();
    process.exit(1);
  }
}

listDatabases();

