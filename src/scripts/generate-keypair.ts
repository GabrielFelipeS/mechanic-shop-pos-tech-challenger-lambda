import { randomUUID } from 'node:crypto';
import { generateKeyPair, exportJWK } from 'jose';

const kid = randomUUID();
const { publicKey, privateKey } = await generateKeyPair('EdDSA');

const publicJwk = { ...(await exportJWK(publicKey)), kid, alg: 'EdDSA', use: 'sig' };
const privateJwk = { ...(await exportJWK(privateKey)), kid, alg: 'EdDSA', use: 'sig' };

console.log('EdDSA keypair generated. Add these to the environment:\n');
console.log(`JWT_PRIVATE_KEY_JWK=${JSON.stringify(privateJwk)}`);
console.log(`JWT_PUBLIC_KEY_JWK=${JSON.stringify(publicJwk)}`);
console.log('\npublic.jwk is served by /.well-known/jwks.json, keep private.jwk secret.');