package org.project.mechanic_shop.cpflogin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CpfLoginRequestHandlerTest {

	private static final String VALID_CPF = "26644152040";

	private CustomerStatusClient statusClient;
	private JwtIssuer jwtIssuer;
	private CpfLoginRequestHandler handler;
	private Context context;

	@BeforeEach
	void setUp() {
		statusClient = mock(CustomerStatusClient.class);
		jwtIssuer = mock(JwtIssuer.class);
		handler = new CpfLoginRequestHandler(statusClient, jwtIssuer);

		context = mock(Context.class);
		when(context.getLogger()).thenReturn(mock(LambdaLogger.class));
	}

	@Test
	void handleRequest_invalidCpf_returns400() {
		var response = handler.handleRequest(requestWithDocument("111.111.111-11"), context);

		assertEquals(400, response.getStatusCode());
	}

	@Test
	void handleRequest_malformedBody_returns400() {
		var event = new APIGatewayProxyRequestEvent().withHttpMethod("POST").withBody("not json");

		var response = handler.handleRequest(event, context);

		assertEquals(400, response.getStatusCode());
	}

	@Test
	void handleRequest_customerNotFoundOrInactive_returns404() throws Exception {
		when(statusClient.fetchStatus(VALID_CPF)).thenReturn(new CustomerStatusClient.CustomerStatus(false, false, null));

		var response = handler.handleRequest(requestWithDocument(VALID_CPF), context);

		assertEquals(404, response.getStatusCode());
	}

	@Test
	void handleRequest_statusServiceUnreachable_returns502() throws Exception {
		when(statusClient.fetchStatus(any())).thenThrow(new java.io.IOException("boom"));

		var response = handler.handleRequest(requestWithDocument(VALID_CPF), context);

		assertEquals(502, response.getStatusCode());
	}

	@Test
	void handleRequest_activeCustomer_returnsTokenFromIssuer() throws Exception {
		when(statusClient.fetchStatus(VALID_CPF))
			.thenReturn(new CustomerStatusClient.CustomerStatus(true, true, "joao.silva@email.com"));
		when(jwtIssuer.issue("joao.silva@email.com")).thenReturn("signed-token");

		APIGatewayProxyResponseEvent response = handler.handleRequest(requestWithDocument(VALID_CPF), context);

		assertEquals(200, response.getStatusCode());
		assertTrue(response.getBody().contains("signed-token"));
	}

	@Test
	void handleRequest_nonPostMethod_returns405() {
		var event = new APIGatewayProxyRequestEvent()
			.withHttpMethod("GET")
			.withBody("{\"document\":\"" + VALID_CPF + "\"}");

		var response = handler.handleRequest(event, context);

		assertEquals(405, response.getStatusCode());
	}

	private APIGatewayProxyRequestEvent requestWithDocument(String document) {
		return new APIGatewayProxyRequestEvent()
			.withHttpMethod("POST")
			.withBody("{\"document\":\"" + document + "\"}");
	}
}
