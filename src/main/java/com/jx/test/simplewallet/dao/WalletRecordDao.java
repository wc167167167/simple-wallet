package com.jx.test.simplewallet.dao;

import com.jx.test.simplewallet.model.WalletRecord;
import javax.management.InvalidAttributeValueException;

public interface WalletRecordDao {
    void insert(WalletRecord newRecord) throws InvalidAttributeValueException;

    void init(WalletRecord initRecord) throws InvalidAttributeValueException;

    WalletRecord latest();
}
