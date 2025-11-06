package com.example.grpc_order_server.server;

import com.example.grpc_order_server.model.OrderStore;
import com.example.grpc_order_server.proto.CreateOrderRequest;
import com.example.grpc_order_server.proto.CreateOrderResponse;
import com.example.grpc_order_server.proto.GetOrderRequest;
import com.example.grpc_order_server.proto.GetOrderResponse;
import com.example.grpc_order_server.proto.OrderServiceGrpc;
import com.example.grpc_order_server.proto.OrderStatusUpdate;
import com.example.grpc_order_server.proto.TrackRequest;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.Instant;

@GrpcService
public class OrderServiceImpl extends OrderServiceGrpc.OrderServiceImplBase {

    private final OrderStore store = OrderStore.getInstance();

    @Override
    public void createOrder(CreateOrderRequest request, StreamObserver<CreateOrderResponse> responseObserver) {
        String id = store.create(request.getProduct(), request.getQuantity(), request.getCustomer());
        CreateOrderResponse resp = CreateOrderResponse.newBuilder()
                .setOrderId(id)
                .setMessage("Order created")
                .build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();

        // Simulate status changes asynchronously
        new Thread(() -> {
            try {
                Thread.sleep(1500);
                store.updateStatus(id, "PROCESSING");
                Thread.sleep(1500);
                store.updateStatus(id, "SHIPPED");
                Thread.sleep(1500);
                store.updateStatus(id, "DELIVERED");
            } catch (InterruptedException ignored) {}
        }).start();
    }

    @Override
    public void getOrder(GetOrderRequest request, StreamObserver<GetOrderResponse> responseObserver) {
        var order = store.get(request.getOrderId());
        if (order == null) {
            responseObserver.onError(new RuntimeException("Order not found"));
            return;
        }
        GetOrderResponse resp = GetOrderResponse.newBuilder()
                .setOrderId(order.id)
                .setProduct(order.product)
                .setQuantity(order.quantity)
                .setCustomer(order.customer)
                .setStatus(order.status)
                .build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void trackOrderStatus(TrackRequest request, StreamObserver<OrderStatusUpdate> responseObserver) {
        String orderId = request.getOrderId();
        long start = System.currentTimeMillis();
        String lastStatus = null;
        while (true) {
            var order = store.get(orderId);
            if (order == null) {
                responseObserver.onError(new RuntimeException("Order not found"));
                return;
            }
            if (!order.status.equals(lastStatus)) {
                OrderStatusUpdate update = OrderStatusUpdate.newBuilder()
                        .setOrderId(order.id)
                        .setStatus(order.status)
                        .setTimestamp(Instant.now().toEpochMilli())
                        .build();
                responseObserver.onNext(update);
                lastStatus = order.status;
                if ("DELIVERED".equals(order.status)) {
                    responseObserver.onCompleted();
                    return;
                }
            }
            try { Thread.sleep(300); } catch (InterruptedException e) { break; }
            if (System.currentTimeMillis() - start > 5 * 60 * 1000) {
                responseObserver.onCompleted();
                return;
            }
        }
    }
}
