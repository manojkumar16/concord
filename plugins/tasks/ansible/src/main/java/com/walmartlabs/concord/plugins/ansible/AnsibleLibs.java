package com.walmartlabs.concord.plugins.ansible;

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

import java.io.IOException;
import java.nio.file.Path;

public class AnsibleLibs {

    private static final Logger log = LoggerFactory.getLogger(AnsibleLibs.class);

    private static final String PYTHON_LIB_DIR = "_python_lib";

    private static final String LIB_LOCATION = "/com/walmartlabs/concord/plugins/ansible/lib";
    private static final String[] LIBS = new String[]{"task_policy.py", "concord_ansible_stats.py"};

    private final Path workDir;
    private final Path tmpDir;

    public AnsibleLibs(Path workDir, Path tmpDir) {
        this.workDir = workDir;
        this.tmpDir = tmpDir;
    }

    public static void process(Path workDir, Path tmpDir, AnsibleEnv env) {
        new AnsibleLibs(workDir, tmpDir).enrichEnv(env).write();
    }

    public AnsibleLibs write() {
        try {
            Resources.copy(LIB_LOCATION, LIBS, tmpDir.resolve(PYTHON_LIB_DIR));
        } catch (IOException e) {
            log.error("write libs error: {}", e.getMessage() );
            throw new RuntimeException("write libs error: " + e.getMessage());
        }

        return this;
    }

    public AnsibleLibs enrichEnv(AnsibleEnv env) {
        env.get().put("PYTHONPATH", workDir.relativize(tmpDir.resolve(PYTHON_LIB_DIR)).toString());
        return this;
    }
}