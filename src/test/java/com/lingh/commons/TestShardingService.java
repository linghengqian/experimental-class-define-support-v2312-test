package com.lingh.commons;

import lombok.Getter;

import javax.sql.DataSource;

@Getter
public final class TestShardingService {

    private final AddressRepository addressRepository;

    public TestShardingService(final DataSource dataSource) {
        addressRepository = new AddressRepository(dataSource);
    }
}
