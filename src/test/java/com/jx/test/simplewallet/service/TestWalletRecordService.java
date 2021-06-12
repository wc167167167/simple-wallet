package com.jx.test.simplewallet.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jx.test.simplewallet.dao.WalletRecordDao;
import com.jx.test.simplewallet.exception.InvalidPaymentException;
import com.jx.test.simplewallet.model.WalletRecord;
import javax.management.InvalidAttributeValueException;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit5.JMockitExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JMockitExtension.class)
class TestWalletRecordService {

    @Test
    void validCoins(@Mocked WalletRecordDao walletRecordDao) throws InvalidAttributeValueException {
        var service = new WalletServiceImpl(walletRecordDao);

        var coins = new int[] { 1, 2, 3 };
        service.init(coins);

        new Verifications() {
            {
                walletRecordDao.init((WalletRecord) any);
                times = 1;
            }
        };
    }

    @Test
    void nullCoins(@Mocked WalletRecordDao walletRecordDao) throws InvalidAttributeValueException {
        var service = new WalletServiceImpl(walletRecordDao);

        assertThrows(InvalidAttributeValueException.class, () -> service.init(null));

        new Verifications() {
            {
                walletRecordDao.init((WalletRecord) any);
                times = 0;
            }
        };
    }

    @Test
    void negativeOrZeroCoins(@Mocked WalletRecordDao walletRecordDao) throws InvalidAttributeValueException {
        var service = new WalletServiceImpl(walletRecordDao);

        assertThrows(InvalidAttributeValueException.class, () -> service.init(new int[] { 1, 0 }));
        assertThrows(InvalidAttributeValueException.class, () -> service.init(new int[] { 1, -1 }));

        new Verifications() {
            {
                walletRecordDao.init((WalletRecord) any);
                times = 0;
            }
        };
    }

    @Test
    void emptyCoins(@Mocked WalletRecordDao walletRecordDao) throws InvalidAttributeValueException {
        var service = new WalletServiceImpl(walletRecordDao);

        assertThrows(InvalidAttributeValueException.class, () -> service.init(new int[] {}));

        new Verifications() {
            {
                walletRecordDao.init((WalletRecord) any);
                times = 0;
            }
        };
    }

    @Test
    void noRecord(@Mocked WalletRecordDao walletRecordDao) throws InvalidAttributeValueException {
        new Expectations() {
            {
                walletRecordDao.latest();
                result = null;
            }
        };

        var service = new WalletServiceImpl(walletRecordDao);
        var result = service.latest();

        assertArrayEquals(new int[] {}, result);
        new Verifications() {
            {
                walletRecordDao.latest();
                times = 1;
            }
        };
    }

    @Test
    void gotRecord(@Mocked WalletRecordDao walletRecordDao) throws InvalidAttributeValueException {
        new Expectations() {
            {
                walletRecordDao.latest();
                result = WalletRecord.builder().content("1,2,3").build();
            }
        };

        var service = new WalletServiceImpl(walletRecordDao);
        var result = service.latest();

        assertArrayEquals(new int[] { 1, 2, 3 }, result);
        new Verifications() {
            {
                walletRecordDao.latest();
                times = 1;
            }
        };
    }

    @Test
    void payNegativeOrZeroCoin(@Mocked WalletRecordDao walletRecordDao) throws InvalidAttributeValueException {
        var service = new WalletServiceImpl(walletRecordDao);

        assertThrows(InvalidAttributeValueException.class, () -> service.pay(-1));
        assertThrows(InvalidAttributeValueException.class, () -> service.pay(0));

        new Verifications() {
            {
                walletRecordDao.insert((WalletRecord) any);
                times = 0;
            }
        };
    }

    @Test
    void payEntireCoin(@Mocked WalletRecordDao walletRecordDao)
        throws InvalidAttributeValueException, InvalidPaymentException {
        new Expectations() {
            {
                walletRecordDao.latest();
                result = WalletRecord.builder().content("1,2,3").total(6).build();
            }
        };

        var service = new WalletServiceImpl(walletRecordDao);
        var result = service.pay(1);

        assertArrayEquals(new int[] { 2, 3 }, result);

        new Verifications() {
            {
                WalletRecord r;
                walletRecordDao.insert(r = withCapture());

                assertEquals(5, r.total());
                assertEquals("2,3", r.content());
            }
        };
    }

    @Test
    void payMultipleCoins(@Mocked WalletRecordDao walletRecordDao)
        throws InvalidAttributeValueException, InvalidPaymentException {
        new Expectations() {
            {
                walletRecordDao.latest();
                result = WalletRecord.builder().content("1,2,3").total(6).build();
            }
        };

        var service = new WalletServiceImpl(walletRecordDao);
        var result = service.pay(3);

        assertArrayEquals(new int[] { 3 }, result);

        new Verifications() {
            {
                WalletRecord r;
                walletRecordDao.insert(r = withCapture());

                assertEquals(3, r.total());
                assertEquals("3", r.content());
            }
        };
    }

    @Test
    void partialCoin(@Mocked WalletRecordDao walletRecordDao)
        throws InvalidAttributeValueException, InvalidPaymentException {
        new Expectations() {
            {
                walletRecordDao.latest();
                result = WalletRecord.builder().content("2,3").total(5).build();
            }
        };

        var service = new WalletServiceImpl(walletRecordDao);
        var result = service.pay(1);

        assertArrayEquals(new int[] { 1, 3 }, result);

        new Verifications() {
            {
                WalletRecord r;
                walletRecordDao.insert(r = withCapture());

                assertEquals(4, r.total());
                assertEquals("1,3", r.content());
            }
        };
    }

    @Test
    void multiWithPartialCoins(@Mocked WalletRecordDao walletRecordDao)
        throws InvalidAttributeValueException, InvalidPaymentException {
        new Expectations() {
            {
                walletRecordDao.latest();
                result = WalletRecord.builder().content("1,2,3").total(6).build();
            }
        };

        var service = new WalletServiceImpl(walletRecordDao);
        var result = service.pay(5);

        assertArrayEquals(new int[] { 1 }, result);

        new Verifications() {
            {
                WalletRecord r;
                walletRecordDao.insert(r = withCapture());

                assertEquals(1, r.total());
                assertEquals("1", r.content());
            }
        };
    }

    @Test
    void finishCoins(@Mocked WalletRecordDao walletRecordDao)
        throws InvalidAttributeValueException, InvalidPaymentException {
        new Expectations() {
            {
                walletRecordDao.latest();
                result = WalletRecord.builder().content("1,2,3").total(6).build();
            }
        };

        var service = new WalletServiceImpl(walletRecordDao);
        var result = service.pay(6);

        assertArrayEquals(new int[] {}, result);

        new Verifications() {
            {
                WalletRecord r;
                walletRecordDao.insert(r = withCapture());

                assertEquals(0, r.total());
                assertEquals("", r.content());
            }
        };
    }

    @Test
    void insufficientCoins(@Mocked WalletRecordDao walletRecordDao)
        throws InvalidAttributeValueException, InvalidPaymentException {
        new Expectations() {
            {
                walletRecordDao.latest();
                result = WalletRecord.builder().content("1,2,3").total(6).build();
            }
        };

        var service = new WalletServiceImpl(walletRecordDao);

        assertThrows(InvalidPaymentException.class, () -> service.pay(7));

        new Verifications() {
            {
                walletRecordDao.insert((WalletRecord) any);
                times = 0;
            }
        };
    }
}
