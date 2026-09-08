package org.project.mechanic_shop.cpflogin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.json.JSONObject;

public class CustomerStatusClient {

	private final HttpClient httpClient = HttpClient.newHttpClient();
	private final String baseUrl;
	private final String internalSecret;

	public CustomerStatusClient(String baseUrl, String internalSecret) {
		this.baseUrl = baseUrl;
		this.internalSecret = internalSecret;
	}

	public record CustomerStatus(boolean exists, boolean active, String email) {
	}

	public CustomerStatus fetchStatus(String document) throws IOException, InterruptedException {
		URI uri = URI.create(baseUrl + "/internal/customers/" + document + "/status");
		HttpRequest request = HttpRequest
			.newBuilder(uri)
			.header("X-Internal-Secret", internalSecret)
			.timeout(Duration.ofSeconds(5))
			.GET()
			.build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			throw new IllegalStateException("Unexpected status from internal endpoint: " + response.statusCode());
		}

		JSONObject json = new JSONObject(response.body());
		return new CustomerStatus(json.getBoolean("exists"), json.getBoolean("active"), json.optString("email", null));
	}
}
