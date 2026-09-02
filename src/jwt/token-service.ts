import { SignJWT, type JWK, type KeyLike } from 'jose';

export interface TokenServiceConfig {
  signingKey: KeyLike;
  publicJwk: JWK;
  issuer: string;
  audience: string;
  ttlSeconds: number;
}

export interface TokenClaims {
  subject: string;
  cpf: string;
}

export class TokenService {
  constructor(private readonly config: TokenServiceConfig) {}

  async sign(claims: TokenClaims): Promise<string> {
    const kid = this.config.publicJwk.kid ?? '';

    return new SignJWT({ cpf: claims.cpf })
      .setProtectedHeader({ alg: 'EdDSA', kid })
      .setSubject(claims.subject)
      .setIssuer(this.config.issuer)
      .setAudience(this.config.audience)
      .setIssuedAt()
      .setExpirationTime(`${this.config.ttlSeconds}s`)
      .sign(this.config.signingKey);
  }

  jwks(): { keys: JWK[] } {
    return { keys: [this.config.publicJwk] };
  }
}