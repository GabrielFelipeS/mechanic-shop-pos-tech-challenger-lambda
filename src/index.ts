import type { APIGatewayProxyEvent, APIGatewayProxyHandler } from 'aws-lambda';
import { Http, type ApiResponse } from './http.js';
import { Router } from './router.js';
import { DatabaseAdapter, type DatabaseConfig } from './database/database-adapter.js';
import { CPFService } from './cpf/cpf-service.js';
import { JwkService } from './jwt/jwk-service.js';
import { TokenService } from './jwt/token-service.js';
import { Controller, type Services } from './controller.js';

function requiredEnv(name: string): string {
  const value = process.env[name];
  if (!value) throw new Error(`Missing required environment variable: ${name}`);
  return value;
}

async function buildServices(): Promise<Services> {
  const dbConfig: DatabaseConfig = {
    host: requiredEnv('DB_HOST'),
    port: Number(process.env.DB_PORT ?? '5432'),
    database: requiredEnv('DB_NAME'),
    user: requiredEnv('DB_USER'),
    password: requiredEnv('DB_PASSWORD'),
    max: Number(process.env.DB_POOL_MAX ?? '1'),
    ssl: process.env.DB_SSL === 'true',
  };

  const database = new DatabaseAdapter(dbConfig);
  const cpfService = new CPFService(database, {
    table: process.env.CPF_TABLE ?? 'customers',
    column: process.env.CPF_COLUMN ?? 'cpf',
  });

  const jwkSet = await JwkService.load();
  const tokenService = new TokenService({
    signingKey: jwkSet.signingKey,
    publicJwk: jwkSet.publicJwk,
    issuer: process.env.JWT_ISSUER ?? 'mechanic-shop-auth',
    audience: process.env.JWT_AUDIENCE ?? 'mechanic-shop',
    ttlSeconds: Number(process.env.JWT_TTL_SECONDS ?? '900'),
  });

  return { cpfService, tokenService };
}

async function buildRouter(): Promise<Router> {
  const services = await buildServices();

  const router = new Router(services);
  router.addPost(/^\/auth\/cpf\/validate$/, Controller.validateCPF);
  router.addGet(/^\/auth\/cpf\/(?<cpf>[0-9]+)\/duplicate$/, Controller.isCPFDuplicated);
  router.addPost(/^\/auth\/token$/, Controller.issueJWT);
  router.addGet(/^\/\.well-known\/jwks\.json$/, Controller.getJWKS);
  return router;
}

let routerPromise: Promise<Router> | null = null;

function getRouter(): Promise<Router> {
  return routerPromise ?? buildRouter();
}

export const handler: APIGatewayProxyHandler = async (
  event: APIGatewayProxyEvent
): Promise<ApiResponse> => {
  try {
    const router = await getRouter();
    return await router.handle(event);
  } catch (error) {
    console.error('Unhandled error', error);
    return Http.response(500, { error: 'Internal server error' });
  }
};