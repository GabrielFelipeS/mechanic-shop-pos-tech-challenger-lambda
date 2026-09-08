package org.project.mechanic_shop.cpflogin;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

/**
 * Dev-only tool: invokes the real handler in-process, using the JWT_SECRET /
 * INTERNAL_API_BASE_URL / INTERNAL_API_SECRET env vars, against whatever
 * backend those point at (e.g. the local docker-compose springapp). No AWS
 * involved. Run via `mvn exec:java` (see this repo's README).
 */
public final class LocalRunner {

	private LocalRunner() {
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: LocalRunner <document>");
			System.exit(1);
		}

		var handler = new CpfLoginRequestHandler();

		var context = mock(Context.class);
		when(context.getLogger()).thenReturn(mock(LambdaLogger.class));

		var event = new APIGatewayProxyRequestEvent()
			.withHttpMethod("POST")
			.withBody("{\"document\":\"" + args[0] + "\"}");

		var response = handler.handleRequest(event, context);

		System.out.println("status: " + response.getStatusCode());
		System.out.println("body:   " + response.getBody());
	}
}
