package com.jx.test.simplewallet.dao;

import com.jx.test.simplewallet.model.WalletRecord;
import javax.management.InvalidAttributeValueException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import org.springframework.stereotype.Repository;

@Repository
public class WalletRecordDaoH2Impl implements WalletRecordDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @Override
    public void insert(WalletRecord newRecord) throws InvalidAttributeValueException {
        if (newRecord == null || newRecord.content() == null || !newRecord.content().matches("^[1-8](,[1-8])*$")) {
            throw new InvalidAttributeValueException("invalid record received");
        }

        if (newRecord.version() <= 1) {
            throw new InvalidAttributeValueException("invalid version");
        }

        var result = 0;
        try {
            result =
                entityManager
                    .createNativeQuery(
                        "INSERT INTO wallet (ts_millis, content, total,version) VALUES (?,?,?, 1 + " +
                        "(SELECT version FROM wallet WHERE version = ? ORDER BY version DESC LIMIT 1))"
                    )
                    .setParameter(1, newRecord.tsMillis())
                    .setParameter(2, newRecord.content())
                    .setParameter(3, newRecord.total())
                    .setParameter(4, newRecord.version() - 1)
                    .executeUpdate();
        } catch (PersistenceException e) {
            throw new InvalidAttributeValueException("version provided invalid");
        }

        if (result == 0) {
            throw new InvalidAttributeValueException("version already updated");
        }
    }

    @Transactional
    @Override
    public void init(WalletRecord newRecord) throws InvalidAttributeValueException {
        if (newRecord == null || newRecord.content() == null || !newRecord.content().matches("^[1-8](,[1-8])*$")) {
            throw new InvalidAttributeValueException("invalid record received");
        }

        if (newRecord.version() != 1) {
            throw new InvalidAttributeValueException("invalid version");
        }

        var result = entityManager
            .createNativeQuery(
                "INSERT INTO wallet (version, ts_millis, content, total) SELECT ?,?,?,? " +
                "WHERE NOT EXISTS (SELECT * FROM wallet);"
            )
            .setParameter(1, newRecord.version())
            .setParameter(2, newRecord.tsMillis())
            .setParameter(3, newRecord.content())
            .setParameter(4, newRecord.total())
            .executeUpdate();

        if (result == 0) {
            throw new InvalidAttributeValueException("wallet already initialised");
        }
    }

    @Override
    public WalletRecord latest() {
        try {
            return (WalletRecord) entityManager
                .createNativeQuery("SELECT * FROM wallet ORDER BY version DESC LIMIT 1", WalletRecord.class)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
