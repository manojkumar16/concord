package com.walmartlabs.concord.server.cfg;

/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2017 - 2018 Walmart Inc.
 * -----
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =====
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

@Named
@Singleton
public class LdapConfiguration implements Serializable {

    public static final String LDAP_CFG_KEY = "LDAP_CFG";
    private static final Logger log = LoggerFactory.getLogger(LdapConfiguration.class);

    private String url;
    private String searchBase;
    private String principalSearchFilter;
    private String userSearchFilter;
    private String usernameProperty;
    private String systemUsername;
    private String systemPassword;
    private Set<String> exposeAttributes;

    public LdapConfiguration() throws IOException {

        String path = System.getenv(LDAP_CFG_KEY);
        if (path != null) {
            Properties props = new Properties();

            try (InputStream in = Files.newInputStream(Paths.get(path))) {
                props.load(in);
            }
            log.info("init -> using external LDAP configuration: {}", path);

            this.url = props.getProperty("url");
            this.searchBase = props.getProperty("searchBase");
            this.principalSearchFilter = props.getProperty("principalSearchFilter");
            this.userSearchFilter = props.getProperty("userSearchFilter");
            this.usernameProperty = props.getProperty("usernameProperty", "sAMAccountName");
            this.systemUsername = props.getProperty("systemUsername");
            this.systemPassword = props.getProperty("systemPassword");
            this.exposeAttributes = split(props, "exposeAttributes");
        } else {
            log.warn("init -> no LDAP configuration");
        }
    }

    private static Set<String> split(Properties props, String key) {
        String s = props.getProperty(key);
        if (s == null || s.isEmpty()) {
            return Collections.emptySet();
        }

        s = s.trim();
        if (s.isEmpty()) {
            return Collections.emptySet();
        }

        String[] as = s.split(",");
        Set<String> result = new HashSet<>(as.length);
        Collections.addAll(result, s.split(","));

        return Collections.unmodifiableSet(result);
    }

    public String getUrl() {
        return url;
    }

    public String getSearchBase() {
        return searchBase;
    }

    public String getPrincipalSearchFilter() {
        return principalSearchFilter;
    }

    public String getUserSearchFilter() {
        return userSearchFilter;
    }

    public String getUsernameProperty() {
        return usernameProperty;
    }

    public String getSystemUsername() {
        return systemUsername;
    }

    public String getSystemPassword() {
        return systemPassword;
    }

    public Set<String> getExposeAttributes() {
        return exposeAttributes;
    }
}
