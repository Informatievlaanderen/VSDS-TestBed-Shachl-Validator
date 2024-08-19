package be.vlaanderen.informatievlaanderen.ldes.ldio;

import be.vlaanderen.informatievlaanderen.ldes.http.RequestExecutor;
import be.vlaanderen.informatievlaanderen.ldes.http.requests.GetRequest;
import be.vlaanderen.informatievlaanderen.ldes.ldio.config.LdioConfigProperties;
import be.vlaanderen.informatievlaanderen.ldes.ldio.valuebojects.ClientStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class LdesClientStatusManager {
	private static final Logger log = LoggerFactory.getLogger(LdesClientStatusManager.class);
	private final RequestExecutor requestExecutor;
	private final LdioConfigProperties ldioConfigProperties;

	public LdesClientStatusManager(RequestExecutor requestExecutor, LdioConfigProperties ldioConfigProperties) {
		this.requestExecutor = requestExecutor;
		this.ldioConfigProperties = ldioConfigProperties;
	}

	public void waitUntilReplicated() {
		final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		final CountDownLatch countDownLatch = new CountDownLatch(1);

		scheduler.scheduleAtFixedRate(() -> {
			try {
				final ClientStatus clientStatus = getClientStatus();
				log.atDebug().log("Checking for LDES client status");
				if (ClientStatus.isSuccessfullyReplicated(clientStatus)) {
					countDownLatch.countDown();
					log.atInfo().log("LDES client status is now {}", clientStatus.toString());
				}
			} catch (Exception e) {
				log.atError().log("Something went wrong while waiting for LDES client to be fully replicated", e);
			}
		}, 0, 5, TimeUnit.SECONDS);

		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.atError().log("Thread interrupted", e);
		} finally {
			scheduler.shutdown();
		}
	}

	public ClientStatus getClientStatus() {
		final String clientStatusUrl = ldioConfigProperties.getLdioLdesClientStatusUrl();
		final HttpEntity response = requestExecutor.execute(new GetRequest(clientStatusUrl));

		final ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.readValue(response.getContent(), ClientStatus.class);
		} catch (IOException e) {
			throw new IllegalStateException("Invalid client status received from %s".formatted(clientStatusUrl));
		}
	}
}
