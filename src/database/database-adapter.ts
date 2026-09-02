import { Pool, type PoolConfig, type QueryResult, type QueryResultRow } from 'pg';

export interface DatabaseConfig {
  host: string;
  port: number;
  database: string;
  user: string;
  password: string;
  max?: number;
  ssl?: boolean;
}

export class DatabaseAdapter {
  private readonly pool: Pool;

  constructor(private readonly config: DatabaseConfig) {
    const poolConfig: PoolConfig = {
      host: config.host,
      port: config.port,
      database: config.database,
      user: config.user,
      password: config.password,
      max: config.max ?? 1,
      ssl: config.ssl ? { rejectUnauthorized: false } : undefined,
    };
    this.pool = new Pool(poolConfig);
  }

  async query<T extends QueryResultRow = QueryResultRow>(
    text: string,
    params: unknown[] = []
  ): Promise<QueryResult<T>> {
    return this.pool.query<T>(text, params);
  }

  async close(): Promise<void> {
    await this.pool.end();
  }
}