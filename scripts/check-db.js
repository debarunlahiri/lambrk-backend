const { Pool } = require('pg');

const DB_HOST = process.env.POSTGRES_HOST || 'localhost';
const DB_PORT = parseInt(process.env.POSTGRES_PORT || '5432');
const DB_USER = process.env.POSTGRES_USER || 'lambrk_user';
const DB_PASSWORD = process.env.POSTGRES_PASSWORD || 'lambrk_password';
const DB_NAME = process.env.POSTGRES_DB || 'lambrk';

const adminPool = new Pool({
  host: DB_HOST,
  port: DB_PORT,
  user: DB_USER,
  password: DB_PASSWORD,
  database: 'postgres',
});

const dbPool = new Pool({
  host: DB_HOST,
  port: DB_PORT,
  user: DB_USER,
  password: DB_PASSWORD,
  database: DB_NAME,
});

async function checkConnection() {
  try {
    await adminPool.query('SELECT 1');
    return { success: true, message: 'PostgreSQL connection successful' };
  } catch (error) {
    return { success: false, message: `Cannot connect to PostgreSQL: ${error.message}` };
  }
}

async function checkDatabaseExists() {
  try {
    const result = await adminPool.query(
      "SELECT 1 FROM pg_database WHERE datname = $1",
      [DB_NAME]
    );
    
    if (result.rows.length > 0) {
      return { exists: true, message: `Database '${DB_NAME}' exists` };
    } else {
      return { exists: false, message: `Database '${DB_NAME}' does not exist` };
    }
  } catch (error) {
    return { exists: false, error: error.message };
  }
}

async function createDatabase() {
  try {
    await adminPool.query(`CREATE DATABASE ${DB_NAME}`);
    return { success: true, message: `Database '${DB_NAME}' created successfully` };
  } catch (error) {
    return { success: false, message: `Failed to create database: ${error.message}` };
  }
}

async function checkTableExists(tableName) {
  try {
    const result = await dbPool.query(
      `SELECT 1 FROM information_schema.tables 
       WHERE table_schema = 'public' AND table_name = $1`,
      [tableName]
    );
    return result.rows.length > 0;
  } catch (error) {
    return false;
  }
}

async function checkRequiredTables() {
  const requiredTables = ['users', 'videos'];
  const results = {};
  
  for (const table of requiredTables) {
    results[table] = await checkTableExists(table);
  }
  
  const allExist = requiredTables.every(table => results[table]);
  const missingTables = requiredTables.filter(table => !results[table]);
  
  return {
    allExist,
    results,
    missingTables,
    message: allExist 
      ? 'All required tables exist' 
      : `Missing tables: ${missingTables.join(', ')}`
  };
}

async function main() {
  const command = process.argv[2];
  
  try {
    switch (command) {
      case 'check-connection':
        const connResult = await checkConnection();
        console.log(JSON.stringify(connResult));
        process.exit(connResult.success ? 0 : 1);
        break;
        
      case 'check-database':
        const dbResult = await checkDatabaseExists();
        console.log(JSON.stringify(dbResult));
        process.exit(dbResult.exists ? 0 : 1);
        break;
        
      case 'create-database':
        const createResult = await createDatabase();
        console.log(JSON.stringify(createResult));
        process.exit(createResult.success ? 0 : 1);
        break;
        
      case 'check-tables':
        const tablesResult = await checkRequiredTables();
        console.log(JSON.stringify(tablesResult));
        process.exit(tablesResult.allExist ? 0 : 1);
        break;
        
      default:
        console.log(JSON.stringify({ error: 'Unknown command' }));
        process.exit(1);
    }
  } catch (error) {
    console.log(JSON.stringify({ error: error.message }));
    process.exit(1);
  } finally {
    await adminPool.end();
    await dbPool.end();
  }
}

main();

