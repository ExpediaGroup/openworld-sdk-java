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
package com.expediagroup.sdk.core.configuration.collector

import com.expediagroup.sdk.core.configuration.provider.ConfigurationProvider

/**
 * A queue of all configuration providers.
 *
 * @property providers List of configuration providers
 */
internal class ConfigurationProviderQueue private constructor(private val providers: List<ConfigurationProvider>) {

    /** Returns the first provider in the queue. */
    fun first(): ConfigurationProvider? = providers.firstOrNull()

    /** Returns the first provider in the queue that matches the given [predicate]. */
    fun first(predicate: (ConfigurationProvider) -> Boolean): ConfigurationProvider? = providers.firstOrNull(predicate)

    /** Returns the configuration from the first provider in the queue that matches the given [predicate].
     *
     * @throws NullPointerException if an instance of the given type [T] is not found in all providers.
     */
    fun <T> firstOf(
        predicate: (provider: ConfigurationProvider) -> T?
    ): T = first { predicate(it) != null }!!.let { predicate(it)!! }

    companion object {
        /** Builds a [ConfigurationProviderQueue] from the given [providers].
         *
         * @param providers the providers to build the queue from.
         * @return a [ConfigurationProviderQueue] instance.
         */
        fun from(providers: List<ConfigurationProvider>) = ConfigurationProviderQueue(providers.toList())
    }
}
