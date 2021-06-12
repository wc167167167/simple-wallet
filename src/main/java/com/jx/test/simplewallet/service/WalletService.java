package com.jx.test.simplewallet.service;

import com.jx.test.simplewallet.exception.InvalidPaymentException;
import javax.management.InvalidAttributeValueException;

public interface WalletService {
    void init(int[] coins) throws InvalidAttributeValueException;

    int[] latest();

    int[] pay(int amount) throws InvalidAttributeValueException, InvalidPaymentException;
}
