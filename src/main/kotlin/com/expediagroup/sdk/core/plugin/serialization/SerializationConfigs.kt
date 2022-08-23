/*
 * Copyright (C) 2022 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.sdk.core.plugin.serialization

import com.expediagroup.sdk.core.plugin.KtorPluginConfigs
import com.expediagroup.sdk.core.plugin.KtorPluginConfigsFactory
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.http.ContentType

data class SerializationConfigs(
    override val httpClientConfig: HttpClientConfig<out HttpClientEngineConfig>,
    val contentType: ContentType = ContentType.Application.Json
) : KtorPluginConfigs(httpClientConfig) {
    companion object : KtorPluginConfigsFactory<SerializationConfigs> {
        override fun from(httpClientConfig: HttpClientConfig<out HttpClientEngineConfig>): SerializationConfigs =
            SerializationConfigs(httpClientConfig)
    }
}