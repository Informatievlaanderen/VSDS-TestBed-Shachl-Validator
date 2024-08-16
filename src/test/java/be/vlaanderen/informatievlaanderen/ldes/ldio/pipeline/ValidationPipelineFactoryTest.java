package be.vlaanderen.informatievlaanderen.ldes.ldio.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationPipelineFactoryTest {
	private static final String LDES_SERVER_URL = "http://test-server/test-collection";
	private static final String SPARQL_HOST = "http://my-sparql-host.net";
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void test_createJson() throws IOException {
		final ValidationPipelineFactory factory = new ValidationPipelineFactory(LDES_SERVER_URL, SPARQL_HOST);
		final JsonNode expectedJson = readJsonNode();

		final String result = factory.createValidationPipelineAsJson();
		final JsonNode actualJson = objectMapper.readTree(result);

		assertThat(actualJson)
				.isEqualTo(expectedJson);

	}

	private JsonNode readJsonNode() throws IOException {
		final var jsonFile = ResourceUtils.getFile("classpath:ldio-pipeline.json");
		return objectMapper.readTree(jsonFile);
	}
}