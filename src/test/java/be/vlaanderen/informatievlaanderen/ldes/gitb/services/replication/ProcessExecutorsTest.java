package be.vlaanderen.informatievlaanderen.ldes.gitb.services.replication;

import be.vlaanderen.informatievlaanderen.ldes.gitb.ldio.LdesClientStatusManager;
import be.vlaanderen.informatievlaanderen.ldes.gitb.ldio.LdioPipelineManager;
import be.vlaanderen.informatievlaanderen.ldes.gitb.rdfrepo.Rdf4jRepositoryManager;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ProcessExecutorsTest {
	private ApplicationContextRunner runner;

	@BeforeEach
	void setUp() {
		runner = new ApplicationContextRunner()
				.withBean(HaltWhenReplicatedProcessExecutor.class, mock(LdesClientStatusManager.class), mock(LdioPipelineManager.class))
				.withBean(StartReplicatingProcessExecutor.class, mock(LdioPipelineManager.class), mock(Rdf4jRepositoryManager.class));
	}

	@Test
	void test_GetExecutors() {
		runner.run(context -> {
			final Collection<ProcessExecutor> executors = new ProcessExecutors(context).getProcessExecutors();

			assertThat(executors)
					.hasSize(2)
					.map(ProcessExecutor::getName)
					.containsExactlyInAnyOrder(StartReplicatingProcessExecutor.NAME, HaltWhenReplicatedProcessExecutor.NAME);
		});
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("expectedBeans")
	void test_Get(String name, Class<? extends ProcessExecutors> beanClass, int numOfParameterDefinitions) {
		runner.run(context -> {
			final Optional<ProcessExecutor> actualProcessExecutor = new ProcessExecutors(context).getProcessExecutor(name);

			assertThat(actualProcessExecutor)
					.containsInstanceOf(beanClass)
					.get()
					.extracting(ProcessExecutor::getParameterDefinitions, InstanceOfAssertFactories.LIST)
					.hasSize(numOfParameterDefinitions);
		});
	}

	@Test
	void test_InvalidGet() {
		runner.run(context -> {
			final Optional<ProcessExecutor> actualProcessExecutor = new ProcessExecutors(context).getProcessExecutor("fantasy");

			assertThat(actualProcessExecutor).isEmpty();
		});
	}

	static Stream<Arguments> expectedBeans() {
		return Stream.of(
				Arguments.of(HaltWhenReplicatedProcessExecutor.NAME, HaltWhenReplicatedProcessExecutor.class, 0),
				Arguments.of(StartReplicatingProcessExecutor.NAME, StartReplicatingProcessExecutor.class, 1)
		);
	}
}