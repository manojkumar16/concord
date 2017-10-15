package com.walmartlabs.concord.server.security.secret;

import com.walmartlabs.concord.common.db.AbstractDao;
import com.walmartlabs.concord.common.secret.SecretStoreType;
import com.walmartlabs.concord.server.api.security.secret.SecretEntry;
import com.walmartlabs.concord.server.api.security.secret.SecretType;
import com.walmartlabs.concord.server.team.TeamDao;
import com.walmartlabs.concord.server.user.UserPermissionCleaner;
import org.jooq.*;
import org.jooq.impl.DSL;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.UUID;

import static com.walmartlabs.concord.server.jooq.tables.Secrets.SECRETS;
import static com.walmartlabs.concord.server.jooq.tables.Teams.TEAMS;
import static org.jooq.impl.DSL.select;

@Named
public class SecretDao extends AbstractDao {

    private final UserPermissionCleaner permissionCleaner;

    @Inject
    public SecretDao(Configuration cfg, UserPermissionCleaner permissionCleaner) {
        super(cfg);
        this.permissionCleaner = permissionCleaner;
    }

    public UUID getId(String name) {
        try (DSLContext tx = DSL.using(cfg)) {
            return tx.select(SECRETS.SECRET_ID)
                    .from(SECRETS)
                    .where(SECRETS.SECRET_NAME.eq(name))
                    .fetchOne(SECRETS.SECRET_ID);
        }
    }

    public String getName(UUID id) {
        try (DSLContext tx = DSL.using(cfg)) {
            return tx.select(SECRETS.SECRET_NAME)
                    .from(SECRETS)
                    .where(SECRETS.SECRET_ID.eq(id))
                    .fetchOne(SECRETS.SECRET_NAME);
        }
    }

    public UUID insert(String name, SecretType type, UUID teamId, SecretStoreType storeType, byte[] data) {
        return txResult(tx -> insert(tx, name, type, teamId, storeType, data));
    }

    public UUID insert(DSLContext tx, String name, SecretType type, UUID teamId, SecretStoreType storeType, byte[] data) {
        if (teamId == null) {
            teamId = TeamDao.DEFAULT_TEAM_ID;
        }

        return tx.insertInto(SECRETS)
                .columns(SECRETS.SECRET_NAME, SECRETS.SECRET_TYPE, SECRETS.TEAM_ID, SECRETS.SECRET_STORE_TYPE, SECRETS.SECRET_DATA)
                .values(name, type.toString(), teamId, storeType.toString(), data)
                .returning(SECRETS.SECRET_ID)
                .fetchOne()
                .getSecretId();
    }

    public SecretDataEntry getByName(String name) {
        try (DSLContext tx = DSL.using(cfg)) {
            return selectSecretDataEntry(tx)
                    .where(SECRETS.SECRET_NAME.eq(name))
                    .fetchOne(SecretDao::toDataEntry);
        }
    }

    public List<SecretEntry> list(Field<?> sortField, boolean asc) {
        Field<String> teamName = select(TEAMS.TEAM_NAME).from(TEAMS).where(TEAMS.TEAM_ID.eq(SECRETS.TEAM_ID)).asField();

        try (DSLContext tx = DSL.using(cfg)) {
            SelectJoinStep<Record6<UUID, String, UUID, String, String, String>> query = tx
                    .select(SECRETS.SECRET_ID,
                            SECRETS.SECRET_NAME,
                            SECRETS.TEAM_ID,
                            teamName,
                            SECRETS.SECRET_TYPE,
                            SECRETS.SECRET_STORE_TYPE)
                    .from(SECRETS);

            if (sortField != null) {
                query.orderBy(asc ? sortField.asc() : sortField.desc());
            }

            return query.fetch(r -> new SecretEntry(r.get(SECRETS.SECRET_ID),
                    r.get(SECRETS.SECRET_NAME),
                    r.get(SECRETS.TEAM_ID),
                    r.get(teamName),
                    SecretType.valueOf(r.get(SECRETS.SECRET_TYPE)),
                    SecretStoreType.valueOf(r.get(SECRETS.SECRET_STORE_TYPE))));
        }
    }

    public void delete(UUID id) {
        tx(tx -> {
            permissionCleaner.onSecretRemoval(tx, getName(id));
            tx.deleteFrom(SECRETS)
                    .where(SECRETS.SECRET_ID.eq(id))
                    .execute();
        });
    }

    private static SelectJoinStep<Record7<UUID, String, UUID, String, String, String, byte[]>> selectSecretDataEntry(DSLContext tx) {
        Field<String> teamName = select(TEAMS.TEAM_NAME).from(TEAMS).where(TEAMS.TEAM_ID.eq(SECRETS.TEAM_ID)).asField();
        return tx.select(
                SECRETS.SECRET_ID,
                SECRETS.SECRET_NAME,
                SECRETS.TEAM_ID,
                teamName,
                SECRETS.SECRET_TYPE,
                SECRETS.SECRET_STORE_TYPE,
                SECRETS.SECRET_DATA)
                .from(SECRETS);
    }

    private static SecretDataEntry toDataEntry(Record7<UUID, String, UUID, String, String, String, byte[]> r) {
        return new SecretDataEntry(r.get(SECRETS.SECRET_ID),
                r.get(SECRETS.SECRET_NAME),
                r.get(SECRETS.TEAM_ID),
                r.get(3, String.class),
                SecretType.valueOf(r.get(SECRETS.SECRET_TYPE)),
                SecretStoreType.valueOf(r.get(SECRETS.SECRET_STORE_TYPE)),
                r.get(SECRETS.SECRET_DATA));
    }

    public static class SecretDataEntry extends SecretEntry {

        private final SecretStoreType storeType;
        private final byte[] data;

        public SecretDataEntry(SecretDataEntry s, byte[] data) {
            this(s.getId(), s.getName(), s.getTeamId(), s.getTeamName(), s.getType(), s.getStoreType(), data);
        }

        public SecretDataEntry(UUID id, String name, UUID teamId, String teamName, SecretType type, SecretStoreType storeType, byte[] data) {
            super(id, name, teamId, teamName, type, storeType);
            this.storeType = storeType;
            this.data = data;
        }

        public SecretStoreType getStoreType() {
            return storeType;
        }

        public byte[] getData() {
            return data;
        }
    }
}
