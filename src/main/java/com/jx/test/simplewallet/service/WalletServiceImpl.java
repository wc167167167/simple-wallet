package com.jx.test.simplewallet.service;

import com.jx.test.simplewallet.dao.WalletRecordDao;
import com.jx.test.simplewallet.exception.InvalidPaymentException;
import com.jx.test.simplewallet.model.WalletRecord;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.management.InvalidAttributeValueException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRecordDao walletRecordDao;

    @Override
    public void init(int[] coins) throws InvalidAttributeValueException {
        if (coins == null || coins.length == 0 || Arrays.stream(coins).anyMatch(c -> c <= 0)) {
            throw new InvalidAttributeValueException("invalid coins");
        }

        var sortedStream = Arrays.stream(coins).sorted().boxed();
        var strBuilder = new StringBuilder();
        var total = new AtomicInteger(0);

        sortedStream.forEach(
            i -> {
                strBuilder.append(i + ",");
                total.set(total.get() + i);
            }
        );
        var content = strBuilder.substring(0, strBuilder.length() - 1);

        walletRecordDao.init(
            WalletRecord
                .builder()
                .version(1)
                .tsMillis(System.currentTimeMillis())
                .content(content)
                .total(total.get())
                .build()
        );
    }

    @Override
    public int[] latest() {
        var latest = walletRecordDao.latest();

        return latest == null
            ? new int[0]
            : Arrays.stream(latest.content().split(",")).map(Integer::parseInt).mapToInt(i -> i).toArray();
    }

    @Override
    public int[] pay(int amount) throws InvalidAttributeValueException, InvalidPaymentException {
        if (amount <= 0) {
            throw new InvalidAttributeValueException("invalid amount");
        }

        var latest = walletRecordDao.latest();
        if (latest == null) {
            throw new InvalidPaymentException("wallet not initialised", new int[0]);
        }

        var coins = Arrays
            .stream(latest.content().split(","))
            .map(Integer::parseInt)
            .collect(Collectors.toCollection(LinkedList::new));

        if (latest.total() < amount) {
            throw new InvalidPaymentException("insufficient fund", coins.stream().mapToInt(i -> i).toArray());
        }

        var coin = 0;
        var amountToPay = amount;
        while (amountToPay > 0) {
            coin = coins.removeFirst();

            var diff = Math.min(amountToPay, coin);
            amountToPay -= diff;
            coin -= diff;
        }

        if (coin > 0) {
            coins.addFirst(coin);
        }

        var content = coins.stream().map(String::valueOf).collect(Collectors.joining(","));

        walletRecordDao.insert(
            WalletRecord
                .builder()
                .version(latest.version() + 1)
                .tsMillis(System.currentTimeMillis())
                .content(content)
                .total(latest.total() - amount)
                .build()
        );

        return coins.stream().mapToInt(i -> i).toArray();
    }
}
