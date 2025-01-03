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

package org.springframework.ai.autoconfigure.bedrock.jurrasic2;

import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;

import org.springframework.ai.autoconfigure.bedrock.BedrockAwsConnectionConfiguration;
import org.springframework.ai.autoconfigure.bedrock.BedrockAwsConnectionProperties;
import org.springframework.ai.bedrock.jurassic2.BedrockAi21Jurassic2ChatModel;
import org.springframework.ai.bedrock.jurassic2.api.Ai21Jurassic2ChatBedrockApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * {@link AutoConfiguration Auto-configuration} for Bedrock Jurassic2 Chat Client.
 *
 * @author Ahmed Yousri
 * @author Wei Jiang
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnClass(Ai21Jurassic2ChatBedrockApi.class)
@EnableConfigurationProperties({ BedrockAi21Jurassic2ChatProperties.class, BedrockAwsConnectionProperties.class })
@ConditionalOnProperty(prefix = BedrockAi21Jurassic2ChatProperties.CONFIG_PREFIX, name = "enabled",
		havingValue = "true")
@Import(BedrockAwsConnectionConfiguration.class)
public class BedrockAi21Jurassic2ChatAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBean({ AwsCredentialsProvider.class, AwsRegionProvider.class })
	public Ai21Jurassic2ChatBedrockApi ai21Jurassic2ChatBedrockApi(AwsCredentialsProvider credentialsProvider,
			AwsRegionProvider regionProvider, BedrockAi21Jurassic2ChatProperties properties,
			BedrockAwsConnectionProperties awsProperties, ObjectMapper objectMapper) {
		return new Ai21Jurassic2ChatBedrockApi(properties.getModel(), credentialsProvider, regionProvider.getRegion(),
				objectMapper, awsProperties.getTimeout());
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBean(Ai21Jurassic2ChatBedrockApi.class)
	public BedrockAi21Jurassic2ChatModel jurassic2ChatModel(Ai21Jurassic2ChatBedrockApi ai21Jurassic2ChatBedrockApi,
			BedrockAi21Jurassic2ChatProperties properties) {

		return BedrockAi21Jurassic2ChatModel.builder(ai21Jurassic2ChatBedrockApi)
			.withOptions(properties.getOptions())
			.build();
	}

}
