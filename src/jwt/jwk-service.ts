import { importJWK, type JWK, type KeyLike } from 'jose';

export interface JwkSet {
  publicJwk: JWK;
  privateJwk: JWK;
  signingKey: KeyLike;
}

function envJwk(name: string): JWK {
  const value = process.env[name];
  if (!value) {
    throw new Error(`Missing required environment variable: ${name}`);
  }
  try {
    return JSON.parse(value) as JWK;
  } catch {
    throw new Error(`Environment variable ${name} is not valid JWK JSON`);
  }
}

export class JwkService {
  static async load(): Promise<JwkSet> {
    const privateJwk = envJwk('JWT_PRIVATE_KEY_JWK');
    const publicJwk = envJwk('JWT_PUBLIC_KEY_JWK');

    const signingKey = (await importJWK(privateJwk, 'EdDSA')) as KeyLike;

    return { publicJwk, privateJwk, signingKey };
  }
}