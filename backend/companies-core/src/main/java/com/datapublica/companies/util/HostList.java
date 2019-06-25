package com.datapublica.companies.util;

import com.google.common.collect.Lists;

import java.util.List;

/**
 *
 */
public class HostList {
    public interface Factory<E> {
        E create(String host, Integer port);
    }

    public static <E> List<E> parse(String hostList, Factory<E> t) {
        String[] hosts = hostList.split(" *, *");
        List<E> addressList = Lists.newArrayList();
        for (String host : hosts) {
            String[] address = host.split(":");
            E a;

            if (address.length == 1) {
                a = t.create(host, null);
            } else if (address.length == 2) {
                a = t.create(address[0], Integer.parseInt(address[1]));
            } else {
                throw new IllegalArgumentException("Invalid hostname: " + host);
            }
            addressList.add(a);
        }
        return addressList;
    }
}
