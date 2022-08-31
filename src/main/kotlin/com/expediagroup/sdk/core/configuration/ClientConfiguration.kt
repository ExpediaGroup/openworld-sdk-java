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
package com.expediagroup.sdk.core.configuration

import com.expediagroup.sdk.core.configuration.provider.ConfigurationProvider

/**
 * SDK Client Configurations.
 *
 * @property key The API key to use for authentication.
 * @property secret The API secret to use for authentication.
 * @property endpoint The API endpoint to use for requests.
 */
class ClientConfiguration private constructor(
    override val key: String? = null,
    override val secret: String? = null,
    override val endpoint: String? = null
) : ConfigurationProvider {

    companion object {

        /** An empty configuration. */
        @JvmField
        val EMPTY = ClientConfiguration()
    }

    /**
     * Builder for [ClientConfiguration].
     */
    class Builder {
        private var key: String? = null
        private var secret: String? = null
        private var endpoint: String? = null

        /** Sets the API key to use for authentication.
         *
         * @param key The API key to use for authentication.
         * @return The [Builder] instance.
         */
        fun key(key: String) = apply { this.key = key }

        /** Sets the API secret to use for authentication.
         *
         * @param secret The API secret to use for authentication.
         * @return The [Builder] instance.
         */
        fun secret(secret: String) = apply { this.secret = secret }

        /** Sets the API endpoint to use for requests.
         *
         * @param endpoint The API endpoint to use for requests.
         * @return The [Builder] instance.
         */
        fun endpoint(endpoint: String) = apply { this.endpoint = endpoint }

        /** Builds the [ClientConfiguration] object.
         *
         * @return The [ClientConfiguration] object.
         */
        fun build() = ClientConfiguration(
            key = key,
            secret = secret,
            endpoint = endpoint
        )
    }
}
