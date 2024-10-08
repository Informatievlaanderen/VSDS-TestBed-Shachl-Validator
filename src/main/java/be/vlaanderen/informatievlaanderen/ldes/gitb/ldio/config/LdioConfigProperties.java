package be.vlaanderen.informatievlaanderen.ldes.gitb.ldio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ldio")
public class LdioConfigProperties {
	private String host;
	private String sparqlHost;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getSparqlHost() {
		return sparqlHost;
	}

	public void setSparqlHost(String sparqlHost) {
		this.sparqlHost = sparqlHost;
	}

	public String getLdioAdminPipelineUrl() {
		return "%s/admin/api/v1/pipeline".formatted(host);
	}

	public String getLdioLdesClientStatusUrlTemplate() {
		return getLdioAdminPipelineUrl() + "/ldes-client/%s";
	}

	public String getRepositoryValidationUrlTemplate() {
		return sparqlHost + "/rest/repositories/%s/validate/text";
	}
}
