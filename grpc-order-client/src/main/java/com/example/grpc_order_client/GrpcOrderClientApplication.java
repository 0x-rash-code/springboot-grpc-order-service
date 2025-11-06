package com.example.grpc_order_client;

import com.example.grpc_order_client.proto.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcOrderClientApplication {

	public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9091).usePlaintext().build();
        OrderServiceGrpc.OrderServiceBlockingStub stub = OrderServiceGrpc.newBlockingStub(channel);

        CreateOrderResponse resp = stub.createOrder(CreateOrderRequest.newBuilder()
                .setProduct("Laptop")
                .setQuantity(1)
                .setCustomer("Rashmi")
                .build());
        System.out.println("Created orderId: " + resp.getOrderId());

        String orderId = resp.getOrderId();

        var stream = stub.trackOrderStatus(TrackRequest.newBuilder().setOrderId(orderId).build());
        while (stream.hasNext()) {
            OrderStatusUpdate update = stream.next();
            System.out.println("Status: " + update.getStatus() + " at " + update.getTimestamp());
        }

        GetOrderResponse go = stub.getOrder(GetOrderRequest.newBuilder().setOrderId(orderId).build());
        System.out.println("Final order status: " + go.getStatus());

        channel.shutdownNow();
    }
}

