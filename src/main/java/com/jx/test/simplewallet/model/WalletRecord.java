package com.jx.test.simplewallet.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity
@Table(name = "wallet")
@Accessors(fluent = true)
@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletRecord {

    @Id
    private long version;

    private long tsMillis;
    private String content;
    private long total;
}
