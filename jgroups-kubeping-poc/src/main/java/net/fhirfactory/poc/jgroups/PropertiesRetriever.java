/*
 * Copyright (c) 2020 ACT Health
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.fhirfactory.poc.jgroups;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Properties;

public class PropertiesRetriever {
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesRetriever.class);

    private static Properties getProperties() {
        return null; //TODO do we want to load defaults from somewhere?
    }

    public String getProperty(String propertyName, String defaultValue) {
        String env = propertyName.toUpperCase(Locale.US);
        env = env.replace(".", "_");
        env = env.replace("-", "_");

        String logMsg = null;

        String value = System.getenv(env);
        if (StringUtils.isNotBlank(value)) {
            logMsg = "Found value in environment variable";
            if (! propertyName.equals(env)) {
                logMsg = logMsg + " " + env;
            }
        } else {
            Properties properties = getProperties();
            if (properties != null) {
                value = properties.getProperty(propertyName);
            }
            if (StringUtils.isNotBlank(value)) {
                logMsg = "Found value in properties";
            } else {
                value = defaultValue;
                logMsg = "Using default value";
            }
        }

        value = value == null ? null : value.trim();

        //Log at warning level so the logs are always shown
        LOG.warn("In PegacornProperties.getProperty(" + propertyName + ") " + logMsg + " " + value);

        return value;
    }

    public String getMandatoryProperty(String propertyName) {
        String value = getProperty(propertyName, StringUtils.EMPTY);
        if (StringUtils.isBlank(value)) {
            throw new IllegalStateException("A value for property " + propertyName + " must be provided");
        }
        return value;
    }

    public Boolean getBooleanProperty(String propertyName, Boolean defaultValue) {
        String value = getProperty(propertyName, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }

    public boolean getBooleanProperty(String propertyName, boolean defaultValue) {
        return getBooleanProperty(propertyName, Boolean.valueOf(defaultValue));
    }

    public Integer getIntegerProperty(String propertyName, Integer defaultValue) {
        String value = getProperty(propertyName, String.valueOf(defaultValue));
        return Integer.parseInt(value);
    }

    /**
     * Sample call is getPropertyEnum("elasticsearch.required_index_status", ElasticsearchIndexStatus.class, ElasticsearchIndexStatus.YELLOW)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public  <T extends Enum> T getPropertyEnum(String thePropertyName, Class<T> theEnumType, T theDefaultValue) {
        String value = getProperty(thePropertyName, theDefaultValue.name());
        return (T) Enum.valueOf(theEnumType, value);
    }
}
