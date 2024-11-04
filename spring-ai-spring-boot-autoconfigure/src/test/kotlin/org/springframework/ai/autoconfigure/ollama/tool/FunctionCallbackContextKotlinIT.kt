/*
 * Copyright 2023-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.springframework.ai.autoconfigure.ollama.tool

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIf
import org.slf4j.LoggerFactory
import org.springframework.ai.autoconfigure.ollama.BaseOllamaIT
import org.springframework.ai.autoconfigure.ollama.OllamaAutoConfiguration
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.model.function.FunctionCallingOptions
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.ai.ollama.api.OllamaOptions
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Description
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@DisabledIf("isDisabled")
class FunctionCallbackContextKotlinIT : BaseOllamaIT() {

	private val logger = LoggerFactory.getLogger(FunctionCallbackContextKotlinIT::class.java)

	private val MODEL_NAME = "qwen2.5:3b"

	val contextRunner = buildConnectionWithModel(MODEL_NAME).let { baseUrl ->
		ApplicationContextRunner().withPropertyValues(
			"spring.ai.ollama.baseUrl=$baseUrl",
			"spring.ai.ollama.chat.options.model=$MODEL_NAME",
			"spring.ai.ollama.chat.options.temperature=0.5",
			"spring.ai.ollama.chat.options.topK=10"
		)
			.withConfiguration(AutoConfigurations.of(OllamaAutoConfiguration::class.java))
			.withUserConfiguration(Config::class.java)
	}

	@Test
	fun functionCallTest() {
		this.contextRunner.run {context ->

			val chatModel = context.getBean(OllamaChatModel::class.java)

			val userMessage = UserMessage(
				"What are the weather conditions in San Francisco, Tokyo, and Paris? Find the temperature in Celsius for each of the three locations.")

			val response = chatModel
					.call(Prompt(listOf(userMessage), OllamaOptions.builder().withFunction("weatherInfo").build()))

			logger.info("Response: " + response)

			assertThat(response.getResult().output.content).contains("30", "10", "15")
		}
	}

	@Test
	fun functionCallWithPortableFunctionCallingOptions() {
		this.contextRunner.run { context ->

			val chatModel = context.getBean(OllamaChatModel::class.java)

			// Test weatherFunction
			val userMessage = UserMessage(
				"What are the weather conditions in San Francisco, Tokyo, and Paris? Find the temperature in Celsius for each of the three locations.")

			val functionOptions = FunctionCallingOptions.builder()
				.withFunction("weatherInfo")
				.build()

			val response = chatModel.call(Prompt(listOf(userMessage), functionOptions));

			logger.info("Response: " + response.getResult().getOutput().getContent());

			assertThat(response.getResult().output.content).contains("30", "10", "15");
		}
	}

	@Configuration
	open class Config {

		@Bean
		@Description("Find the weather conditions, forecasts, and temperatures for a location, like a city or state.")
		open fun weatherInfo(): (KotlinRequest) -> KotlinResponse = { request ->
			var temperature = 10.0
			if (request.location.contains("Paris")) {
				temperature = 15.0
			}
			else if (request.location.contains("Tokyo")) {
				temperature = 10.0
			}
			else if (request.location.contains("San Francisco")) {
				temperature = 30.0
			}
			KotlinResponse(temperature, 15.0, 20.0, 2.0, 53, 45, Unit.C);
		}

	}
}