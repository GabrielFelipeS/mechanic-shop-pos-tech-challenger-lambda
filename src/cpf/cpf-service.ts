import type { DatabaseAdapter } from '../database/database-adapter.js';

export interface CPFRepositoryConfig {
  table: string;
  column: string;
}

export class CPFService {
  constructor(
    private readonly db: DatabaseAdapter,
    private readonly config: CPFRepositoryConfig
  ) {}

  async isDuplicate(cpf: string): Promise<boolean> {
    const result = await this.db.query(
      `SELECT 1 FROM ${this.config.table} WHERE ${this.config.column} = $1 LIMIT 1`,
      [cpf]
    );
    return (result.rowCount ?? 0) > 0;
  }
}