package com.walmartlabs.concord.server.events;

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


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GithubUtils {

    /**
     *
     * Same rules as used by git in shorten_unambiguous_ref
     * see: https://github.com/git/git/blob/v2.19.1/refs.c#L483
     */
    private static final Pattern[] REF_PARSE_RULES = {
            Pattern.compile("^(.*)$"),
            Pattern.compile("^refs/(.*)$"),
            Pattern.compile("^refs/tags/(.*)$"),
            Pattern.compile("^refs/heads/(.*)$"),
            Pattern.compile("^refs/remotes/(.*)$"),
            Pattern.compile("^refs/remotes/(.*)/HEAD$")
    };

    public static String getRefShortName(String ref) {
        String str = ref.trim();
        String result = str;
        for (Pattern p : REF_PARSE_RULES) {
            Matcher m = p.matcher(str);
            if (m.matches()) {
                result = m.group(1);
            }
        }
        return result;
    }

    public static boolean isRepositoryUrl(String repo, String url, String githubDomain) {
        // TODO necessary for tests
        if (repo.equals(url)) {
            return true;
        }

        String pattern = ".*" + Pattern.quote(githubDomain) + "[:/]" + Pattern.quote(repo) + "(?:.git|.git/|/)?";
        return Pattern.compile(pattern).matcher(url).matches();
    }

    public static String getRepositoryName(String repoUrl) {
        String repoPath = getRepoPath(repoUrl);

        String[] u = repoPath.split("/");
        if(u.length < 2) {
            return null;
        }
        return owner(u[0]) + "/" + name(u[1]);
    }

    private static String getRepoPath(String repoUrl) {
        String u = removeSchema(repoUrl);
        u = removeHost(u);
        return u;
    }

    private static String removeSchema(String repoUrl) {
        int index = repoUrl.indexOf("://");
        if (index > 0) {
            return repoUrl.substring(index + "://".length());
        }
        index = repoUrl.indexOf("@");
        if (index > 0) {
            return repoUrl.substring(index + "@".length());
        }
        return repoUrl;
    }

    private static String removeHost(String repoUrl) {
        int index = repoUrl.indexOf(":");
        if (index > 0) {
            int portEndIndex = repoUrl.indexOf("/", index);
            if(portEndIndex > 0) {
                String port = repoUrl.substring(index + 1, portEndIndex);
                if (isPort(port)) {
                    return repoUrl.substring(portEndIndex + "/".length());
                }
            }
            return repoUrl.substring(index + ":".length());
        }
        index = repoUrl.indexOf("/");
        if (index > 0) {
            return repoUrl.substring(index + "/".length());
        }
        return repoUrl;
    }

    private static String name(String str) {
        return str.replaceAll("^\\W+|\\.git$", "");
    }

    private static String owner(String str) {
        int idx = str.indexOf(':');
        if (idx > 0) {
            return str.substring(idx + 1);
        }
        return str;
    }

    private static boolean isPort(String str) {
        try {
            int port = Integer.parseInt(str);
            return port > 0 && port <= 65535;
        } catch(NumberFormatException e) {
            return false;
        }
    }

    private GithubUtils() {
    }
}
