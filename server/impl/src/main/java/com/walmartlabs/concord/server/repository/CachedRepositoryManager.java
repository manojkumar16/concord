package com.walmartlabs.concord.server.repository;

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

import com.walmartlabs.concord.db.AbstractDao;
import com.walmartlabs.concord.server.org.project.RepositoryEntry;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Callable;

import static com.walmartlabs.concord.server.jooq.tables.Repositories.REPOSITORIES;
import static com.walmartlabs.concord.server.repository.RepositoryMetaManager.RepositoryMeta;

public class CachedRepositoryManager implements RepositoryManager {

    private static final Logger log = LoggerFactory.getLogger(CachedRepositoryManager.class);

    private final RepositoryMetaManager repositoryMetaManager;

    private final RepositoryManager delegate;

    private final RepositoryCacheDao repositoryDao;

    public CachedRepositoryManager(RepositoryMetaManager repositoryMetaManager,
                                   RepositoryManager delegate,
                                   RepositoryCacheDao repositoryDao) {
        this.repositoryMetaManager = repositoryMetaManager;
        this.delegate = delegate;
        this.repositoryDao = repositoryDao;
    }

    @Override
    public void testConnection(UUID orgId, String uri, String branch, String commitId, String path, String secret) {
        delegate.testConnection(orgId, uri, branch, commitId, path, secret);
    }

    @Override
    public <T> T withLock(UUID projectId, String repoName, Callable<T> f) {
        return delegate.withLock(projectId, repoName, f);
    }

    @Override
    public Path fetch(UUID projectId, RepositoryEntry repository) {
        String repoName = repository.getName();
        return withLock(projectId, repoName, () -> {
            Date lastPushDate = repositoryDao.getLastPushDate(projectId, repoName);
            RepositoryMeta rm = repositoryMetaManager.readMeta(projectId, repository);
            if (!repository.isHasWebHook() || needUpdate(rm, lastPushDate)) {
                Path result = delegate.fetch(projectId, repository);
                repositoryMetaManager.writeMeta(projectId, repository, new RepositoryMeta(lastPushDate));
                log.info("fetch ['{}', '{}'] -> updated", projectId, repoName);
                return result;
            } else {
                log.info("fetch ['{}', '{}'] -> from cache", projectId, repoName);
                return delegate.getRepoPath(projectId, repository);
            }
        });
    }

    @Override
    public Path getRepoPath(UUID projectId, RepositoryEntry repository) {
        return delegate.getRepoPath(projectId, repository);
    }

    @Override
    public RepositoryInfo getInfo(RepositoryEntry repository, Path path) {
        return delegate.getInfo(repository, path);
    }

    private boolean needUpdate(RepositoryMeta rm, Date lastPushDate) {
        return rm == null || rm.getPushDate().before(lastPushDate);
    }

    @Named
    public static class RepositoryCacheDao extends AbstractDao {

        @Inject
        public RepositoryCacheDao(Configuration cfg) {
            super(cfg);
        }

        public Date getLastPushDate(UUID projectId, String repoName) {
            try (DSLContext tx = DSL.using(cfg)) {
                return tx.select(REPOSITORIES.PUSH_EVENT_DATE)
                        .from(REPOSITORIES)
                        .where(REPOSITORIES.PROJECT_ID.eq(projectId)
                                .and(REPOSITORIES.REPO_NAME.eq(repoName)))
                        .fetchOne(REPOSITORIES.PUSH_EVENT_DATE);
            }
        }

        public boolean updateLastPushDate(UUID repoId, Date pushDate) {
            return txResult(tx -> tx.update(REPOSITORIES)
                    .set(REPOSITORIES.PUSH_EVENT_DATE, new Timestamp(pushDate.getTime()))
                    .where(REPOSITORIES.REPO_ID.eq(repoId))
                    .execute() == 1);
        }
    }
}
