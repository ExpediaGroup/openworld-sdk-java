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
package com.expediagroup.sdk.core.config

import com.expediagroup.sdk.core.constant.ExceptionMessage.configurationDefinedTwice
import com.expediagroup.sdk.core.constant.ExceptionMessage.configurationKeyNotDefined
import com.expediagroup.sdk.core.constant.ExceptionMessage.expectedActualNameValue
import com.expediagroup.sdk.core.constant.ExceptionMessage.expectedNameValue
import com.expediagroup.sdk.core.constant.ExceptionMessage.requiredConfigurationsNotDefined
import com.expediagroup.sdk.core.model.exception.ConfigurationException
import org.slf4j.LoggerFactory
import java.util.Locale

/**
 * A definition of a configuration property.
 */
class ConfigurationDefinition {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var configKeys = mutableMapOf<String, ConfigurationKey>()

    /**
     * Define a new configuration which is required by the SDK.
     *
     * @param key key which needs to be defined
     * @return ConfigurationDefinition object after adding the configuration key
     */
    private fun define(key: ConfigurationKey): ConfigurationDefinition {
        if (configKeys.containsKey(key.name))
            throw ConfigurationException(configurationDefinedTwice(key.name))
                .also { logger.error(it.message) }
        configKeys[key.name] = key
        return this
    }

    /**
     * Define a new configuration which is required by the SDK.
     *
     * @param name of the configuration, it should be unique, use a . delimited string eg : api_credentials.client_key
     * @param documentation documentation for the configuration key, its logged and used by the client of the sdk to set the right value
     * @param type type of the expected configuration value.
     * @param importance specifies the importance of the key, sdk fails to initialize if a configuration for a key of high importance is not passed
     * @param defaultValue the default value of the configuration if it's not passed, it's an optional field, some key's won't have a default
     * @param validator - configuration validator if the value needs additional validation. eg : if a value has to be between 1-10 just expecting the type to be INT would not be enough
     * @return ConfigurationDefinition object after adding the configuration key
     */

    // TODO: Fix the cause and remove the suppression warning.
    @Suppress("LongParameterList")
    fun define(
        name: String,
        documentation: String,
        type: ConfigurationKey.Type,
        importance: ConfigurationKey.Importance,
        defaultValue: Any? = null,
        validator: ConfigurationKey.Validator? = null
    ): ConfigurationDefinition {
        val key = ConfigurationKey(
            name = name,
            documentation = documentation,
            type = type,
            importance = importance,
            defaultValue = defaultValue,
            validator = validator
        )
        return define(key)
    }

    /**
     * Gets the configuration key given the unique identifier.
     *
     * @param name - identifier for the configuration key
     * @return - configuration key
     */
    fun get(name: String): ConfigurationKey =
        configKeys[name] ?: throw ConfigurationException(configurationKeyNotDefined(name))
            .also { logger.error(it.message) }

    /**
     * Parses the configuration values based on the defined keys.
     *
     * @param props - unparsed configuration values as properties
     * @return - map of configuration names along with its parsed values
     */
    fun parse(props: Map<String, Any>): Map<String, Any> {
        // Check all configurations are defined
        val undefinedConfigKeys: List<String> = undefinedConfigs(props)
        if (undefinedConfigKeys.isNotEmpty())
            throw ConfigurationException(requiredConfigurationsNotDefined(undefinedConfigKeys.joinToString(",")))
                .also { logger.error(it.message) }
        // parse all known keys
        val values: MutableMap<String, Any> = HashMap()
        for (key in configKeys.values) values[key.name] =
            parseValue(key, props[key.name])
        return values
    }

    private fun undefinedConfigs(props: Map<String, Any>): List<String> {
        val importantKeys = configKeys.values.filter { key ->
            run {
                key.defaultValue == null && key.importance == ConfigurationKey.Importance.HIGH
            }
        }.map { it.name }
        return importantKeys.filterNot { props.keys.contains(it) }
    }

    private fun parseValue(key: ConfigurationKey, value: Any?): Any {
        return parseType(key.name, run { value ?: key.defaultValue }!!, key.type)
            .let { key.validator?.ensureValid(key.name, it) ?: it }
    }

    // TODO: Fix the causes and remove the suppression warning.
    @Suppress("ComplexMethod", "ThrowsCount")
    private fun parseType(name: String, value: Any, type: ConfigurationKey.Type): Any {
        return when (type) {
            ConfigurationKey.Type.BOOLEAN -> parseBoolean(value, name)
            ConfigurationKey.Type.PASSWORD -> parsePassword(value, name)
            ConfigurationKey.Type.STRING -> parseString(value, name)
            ConfigurationKey.Type.INT -> parseInt(value, name)
            ConfigurationKey.Type.DOUBLE -> parseDouble(value, name)
            ConfigurationKey.Type.LIST -> parseList(value, name)
        }
    }

    private fun parseBoolean(value: Any, name: String): Boolean {
        toBooleanOrNull(value)?.let { return it }

        throw ConfigurationException(expectedNameValue("boolean", name, value))
            .also { logger.error(it.message) }
    }

    private fun parsePassword(value: Any, name: String): ConfigurationKey.Password {
        toPasswordOrNull(value)?.let { return it }

        throw ConfigurationException(expectedActualNameValue("string", value.javaClass.name, name, value))
            .also { logger.error(it.message) }
    }

    private fun parseString(value: Any, name: String): String {
        toStringOrNull(value)?.let { return it }

        throw ConfigurationException(expectedActualNameValue("string", value.javaClass.name, name, value))
            .also { logger.error(it.message) }
    }

    private fun parseInt(value: Any, name: String): Int {
        toIntOrNull(value)?.let { return it }

        throw ConfigurationException(expectedActualNameValue("32-bit integer", value.javaClass.name, name, value))
            .also { logger.error(it.message) }
    }

    private fun parseDouble(value: Any, name: String): Double {
        toDoubleOrNull(value)?.let { return it }

        throw ConfigurationException(expectedActualNameValue("double", value.javaClass.name, name, value))
            .also { logger.error(it.message) }
    }

    private fun parseList(value: Any, name: String): List<*> {
        toListOrNull(value)?.let { return it }

        throw ConfigurationException(expectedNameValue("comma-separated list", name, value))
            .also { logger.error(it.message) }
    }

    private fun toBooleanOrNull(value: Any) = when (value) {
        is String -> value.lowercase(Locale.getDefault()).toBooleanStrictOrNull()
        is Boolean -> value
        else -> null
    }

    private fun toPasswordOrNull(value: Any) =
        (value as? ConfigurationKey.Password) ?: if (value is String) ConfigurationKey.Password(value.trim()) else null

    private fun toStringOrNull(value: Any) = if (value is String) value.trim() else null

    private fun toIntOrNull(value: Any) = when (value) {
        is Number -> value.toInt()
        is String -> value.toIntOrNull()
        else -> null
    }

    private fun toDoubleOrNull(value: Any) = when (value) {
        is Number -> value.toDouble()
        is String -> value.toDoubleOrNull()
        else -> null
    }

    private fun toListOrNull(value: Any) = (value as? List<*>)
        ?: if (value is String) if (value.isEmpty()) emptyList<Any>() else value.split(",") else null
}
