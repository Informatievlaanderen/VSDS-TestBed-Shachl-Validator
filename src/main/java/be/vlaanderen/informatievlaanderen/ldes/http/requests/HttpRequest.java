package be.vlaanderen.informatievlaanderen.ldes.http.requests;

import org.apache.http.client.methods.HttpRequestBase;

public interface HttpRequest {
	String getUrl();
	HttpRequestBase createRequest();
}
