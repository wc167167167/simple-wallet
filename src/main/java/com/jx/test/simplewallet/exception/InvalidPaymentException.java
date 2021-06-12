package com.jx.test.simplewallet.exception;

import lombok.Getter;

@Getter
public class InvalidPaymentException extends Exception {

    private final int[] leftCoins;

    public InvalidPaymentException(String message, int[] leftCoins) {
        super(message);
        this.leftCoins = leftCoins;
    }
}
