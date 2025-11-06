package com.example.grpc_order_server.model;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OrderStore {

    public static class Order {
        public final String id;
        public final String product;
        public final int quantity;
        public final String customer;
        public volatile String status;

        public Order(String id, String product, int quantity, String customer, String status) {
            this.id = id;
            this.product = product;
            this.quantity = quantity;
            this.customer = customer;
            this.status = status;
        }
    }

    private final Map<String, Order> map = new ConcurrentHashMap<>();
    private static final OrderStore INSTANCE = new OrderStore();

    private OrderStore() {}
    public static OrderStore getInstance() { return INSTANCE; }

    public String create(String product, int quantity, String customer) {
        String id = UUID.randomUUID().toString();
        Order o = new Order(id, product, quantity, customer, "CREATED");
        map.put(id, o);
        return id;
    }

    public Order get(String id) {
        return map.get(id);
    }

    public void updateStatus(String id, String status) {
        Order o = map.get(id);
        if (o != null) o.status = status;
    }
}
