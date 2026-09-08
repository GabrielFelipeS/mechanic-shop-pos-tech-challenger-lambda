package org.project.mechanic_shop.cpflogin;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class JwtIssuer {

	private final String secret;
	private final String issuer;

	public JwtIssuer(String secret, String issuer) {
		this.secret = secret;
		this.issuer = issuer;
	}

	public String issue(String email) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);
			return JWT
				.create()
				.withIssuer(issuer)
				.withSubject(email)
				.withExpiresAt(Instant.now().plus(2, ChronoUnit.HOURS))
				.sign(algorithm);
		} catch (JWTCreationException exception) {
			throw new IllegalStateException("Error while generating token", exception);
		}
	}
}
