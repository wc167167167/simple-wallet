package com.jx.test.simplewallet.dao;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jx.test.simplewallet.model.WalletRecord;
import javax.management.InvalidAttributeValueException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Transactional
@SpringBootTest
class TestWalletRecordDao {

    @Autowired
    DataSource dataSource;

    @Autowired
    WalletRecordDao walletRecordDao;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @AfterEach
    void cleanUp() {
        entityManager.createNativeQuery("DELETE FROM wallet").executeUpdate();
    }

    @Test
    void canInit() {
        var record = WalletRecord
            .builder()
            .version(1)
            .tsMillis(System.currentTimeMillis())
            .content("2,3,1,2,1")
            .total(9)
            .build();

        assertDoesNotThrow(() -> walletRecordDao.init(record));
    }

    @Test
    void cannotReinit() {
        var record = WalletRecord
            .builder()
            .version(1)
            .tsMillis(System.currentTimeMillis())
            .content("2,3,1,2,1")
            .total(9)
            .build();

        assertDoesNotThrow(() -> walletRecordDao.init(record));

        assertThrows(
            InvalidAttributeValueException.class,
            () -> {
                walletRecordDao.init(record);
            }
        );
    }

    @Test
    void cannotInitWithNegativeValues() {
        var record = WalletRecord
            .builder()
            .version(1)
            .tsMillis(System.currentTimeMillis())
            .content("-2")
            .total(9)
            .build();

        assertThrows(
            InvalidAttributeValueException.class,
            () -> {
                walletRecordDao.init(record);
            }
        );
    }

    @Test
    void cannotInitWithNull() {
        assertThrows(
            InvalidAttributeValueException.class,
            () -> {
                walletRecordDao.init(null);
            }
        );
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "1,1.5,2", "1,1,", "1,-1,3", "\"1\"" })
    void cannotInitWithInvalidContent(String content) {
        var record = WalletRecord
            .builder()
            .version(1)
            .tsMillis(System.currentTimeMillis())
            .content(content)
            .total(9)
            .build();

        assertThrows(
            InvalidAttributeValueException.class,
            () -> {
                walletRecordDao.init(record);
            }
        );
    }

    @Test
    void cannotInitWithNullContent() {
        var record = WalletRecord
            .builder()
            .version(1)
            .tsMillis(System.currentTimeMillis())
            .content(null)
            .total(9)
            .build();

        assertThrows(
            InvalidAttributeValueException.class,
            () -> {
                walletRecordDao.init(record);
            }
        );
    }

    @ParameterizedTest
    @ValueSource(ints = { -1, 0, 2 })
    void cannotInitWithInvalidVersion(int version) {
        var record = WalletRecord
            .builder()
            .version(version)
            .tsMillis(System.currentTimeMillis())
            .content("1")
            .total(9)
            .build();

        assertThrows(
            InvalidAttributeValueException.class,
            () -> {
                walletRecordDao.init(record);
            }
        );
    }

    @Test
    void insertWithCorrectVersion() {
        var record1 = WalletRecord
            .builder()
            .version(1)
            .tsMillis(System.currentTimeMillis())
            .content("1")
            .total(9)
            .build();

        var record2 = WalletRecord
            .builder()
            .version(2)
            .tsMillis(System.currentTimeMillis())
            .content("1")
            .total(9)
            .build();

        var record3 = WalletRecord
            .builder()
            .version(3)
            .tsMillis(System.currentTimeMillis())
            .content("1")
            .total(9)
            .build();

        assertDoesNotThrow(
            () -> {
                walletRecordDao.init(record1);
                walletRecordDao.insert(record2);
                walletRecordDao.insert(record3);
            }
        );
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -1, 4 })
    void insertWithWrongVersion(long version) {
        var record1 = WalletRecord
            .builder()
            .version(1)
            .tsMillis(System.currentTimeMillis())
            .content("1")
            .total(9)
            .build();

        var record2 = WalletRecord
            .builder()
            .version(2)
            .tsMillis(System.currentTimeMillis())
            .content("1")
            .total(9)
            .build();

        var record3 = WalletRecord
            .builder()
            .version(version)
            .tsMillis(System.currentTimeMillis())
            .content("1")
            .total(9)
            .build();

        assertDoesNotThrow(
            () -> {
                walletRecordDao.init(record1);
                walletRecordDao.insert(record2);
            }
        );

        assertThrows(InvalidAttributeValueException.class, () -> walletRecordDao.insert(record3));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "1,1.5,2", "1,1,", "1,-1,3", "\"1\"" })
    void cannotInsertWithInvalidContent(String content) {
        var record1 = WalletRecord
            .builder()
            .version(1)
            .tsMillis(System.currentTimeMillis())
            .content("1")
            .total(9)
            .build();

        var record2 = WalletRecord
            .builder()
            .version(2)
            .tsMillis(System.currentTimeMillis())
            .content(content)
            .total(9)
            .build();

        assertDoesNotThrow(() -> walletRecordDao.init(record1));

        assertThrows(
            InvalidAttributeValueException.class,
            () -> {
                walletRecordDao.insert(record2);
            }
        );
    }

    @Test
    void cannotInsertWithNullContent() {
        var record1 = WalletRecord
            .builder()
            .version(1)
            .tsMillis(System.currentTimeMillis())
            .content("1")
            .total(9)
            .build();

        var record2 = WalletRecord
            .builder()
            .version(2)
            .tsMillis(System.currentTimeMillis())
            .content(null)
            .total(9)
            .build();

        assertDoesNotThrow(() -> walletRecordDao.init(record1));

        assertThrows(
            InvalidAttributeValueException.class,
            () -> {
                walletRecordDao.insert(record2);
            }
        );
    }
}
