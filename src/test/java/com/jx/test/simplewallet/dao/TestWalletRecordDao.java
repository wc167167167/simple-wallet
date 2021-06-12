package com.jx.test.simplewallet.dao;

import com.jx.test.simplewallet.model.WalletRecord;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.management.InvalidAttributeValueException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TestWalletRecordDao {

    @Autowired
    DataSource dataSource;

    @Autowired
    WalletRecordDao walletRecordDao;

    @Test
    void testInit() throws InvalidAttributeValueException, SQLException {
        DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
        ResultSet tables = metaData.getTables(null, null, null, new String[] { "TABLE" });
        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            System.out.println(tableName);
            ResultSet columns = metaData.getColumns(null, null, tableName, "%");
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                System.out.println("\t" + columnName);
            }
        }

        System.out.println("----------------------------------------------------------------------------------------");

        var record = WalletRecord
            .builder()
            .version(1)
            .tsMillis(System.currentTimeMillis())
            .content("[2,3,1,2,1]")
            .total(9)
            .build();
        walletRecordDao.init(record);
    }
}
