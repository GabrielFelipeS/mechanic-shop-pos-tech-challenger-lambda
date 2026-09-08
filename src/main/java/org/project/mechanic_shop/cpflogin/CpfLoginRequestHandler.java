package org.project.mechanic_shop.cpflogin;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class CpfLoginRequestHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private static final Map<String, String> JSON_HEADERS = Map.of("Content-Type", "application/json");

	private final CustomerStatusClient statusClient;
	private final JwtIssuer jwtIssuer;

	// no-args constructor is required: this is what the Lambda runtime instantiates in production.
	public CpfLoginRequestHandler() {
		this(
			new CustomerStatusClient(requireEnv("INTERNAL_API_BASE_URL"), requireEnv("INTERNAL_API_SECRET")),
			new JwtIssuer(requireEnv("JWT_SECRET"), System.getenv().getOrDefault("JWT_ISSUER", "mechanic-shop-api"))
		);
	}

	CpfLoginRequestHandler(CustomerStatusClient statusClient, JwtIssuer jwtIssuer) {
		this.statusClient = statusClient;
		this.jwtIssuer = jwtIssuer;
	}

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
		if (input.getHttpMethod() != null && !"POST".equalsIgnoreCase(input.getHttpMethod())) {
			return response(405, error("Method not allowed."));
		}

		String document;
		try {
			document = new JSONObject(input.getBody()).getString("document").replaceAll("\\D", "");
		} catch (JSONException | NullPointerException exception) {
			return response(400, error("Request body must be JSON with a 'document' field."));
		}

		if (!CpfValidator.isValid(document)) {
			return response(400, error("Invalid CPF."));
		}

		CustomerStatusClient.CustomerStatus status;
		try {
			status = statusClient.fetchStatus(document);
		} catch (Exception exception) {
			context.getLogger().log("Failed to reach customer status service: " + exception.getMessage());
			return response(502, error("Failed to reach customer status service."));
		}

		if (!status.exists() || !status.active()) {
			return response(404, error("Customer not found or inactive."));
		}

		String token = jwtIssuer.issue(status.email());
		return response(200, new JSONObject().put("token", token).toString());
	}

	private static String requireEnv(String name) {
		String value = System.getenv(name);
		if (value == null || value.isBlank()) {
			throw new IllegalStateException("Missing required environment variable: " + name);
		}
		return value;
	}

	private String error(String message) {
		return new JSONObject().put("error", message).toString();
	}

	private APIGatewayProxyResponseEvent response(int status, String body) {
		return new APIGatewayProxyResponseEvent()
			.withStatusCode(status)
			.withHeaders(JSON_HEADERS)
			.withBody(body);
	}
}
