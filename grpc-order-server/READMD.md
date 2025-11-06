# 🧩 gRPC Order Server — Spring Boot 3.4.x + Java 21 + Gradle

A simple **gRPC-based Order Service** built with **Spring Boot 3.4**, **Java 21**, and the **Protobuf Gradle Plugin**.
This guide explains how to **set up, generate, and run** your gRPC server step-by-step.

---

## 🏗️ Project Overview

```
grpc-order-server/
├── build.gradle
├── settings.gradle
├── src/
│   ├── main/
│   │   ├── java/com/example/grpc_order_server/
│   │   │   ├── GrpcOrderApplication.java
│   │   │   ├── model/OrderStore.java
│   │   │   └── server/OrderServiceImpl.java
│   │   ├── proto/order.proto       <-- gRPC contract
│   │   └── resources/application.yml
└── build/generated/source/proto/   <-- Generated files (after running `generateProto`)
```

---

## ⚙️ 1️⃣ Prerequisites

| Tool            | Version |
| --------------- | ------- |
| Java            | 21      |
| Spring Boot     | 3.4.x   |
| Gradle          | 8.5+    |
| Protobuf Plugin | 0.9.4   |

---

## 🧠 2️⃣ About Code Generation

When you define a `.proto` file (like `src/main/proto/order.proto`),
the **Protobuf Gradle Plugin** automatically generates Java classes and gRPC stubs when you run:

```bash
./gradlew generateProto
```

This command compiles the `.proto` file and creates Java classes under:

```
build/generated/source/proto/main/java/
build/generated/source/proto/main/grpc/
```

Those generated files include:

* **Message Classes** → `CreateOrderRequest.java`, `GetOrderResponse.java`, etc.
* **Service Stub** → `OrderServiceGrpc.java` (used by your `OrderServiceImpl`).

---

## ⚙️ 3️⃣ Steps to Build and Run

### 🔹 Step 1: Generate the gRPC Java files

Run this command first:

```bash
./gradlew generateProto --info
```

✅ What it does:

* Compiles `src/main/proto/order.proto`
* Creates generated Java classes in `build/generated/source/proto/...`

Verify the files exist:

```bash
find build/generated/source/proto -type f
```

Expected output example:

```
build/generated/source/proto/main/java/com/example/grpc_order_server/proto/CreateOrderRequest.java
build/generated/source/proto/main/grpc/com/example/grpc_order_server/proto/OrderServiceGrpc.java
```

---

### 🔹 Step 2: Mark folders as source roots (IntelliJ)

In **IntelliJ IDEA**:

1. Go to `build/generated/source/proto/main/java`
2. Right-click → **Mark Directory As → Generated Sources Root**
3. Do the same for `build/generated/source/proto/main/grpc`
4. Then click **Reload All Gradle Projects**

🟦 These folders should turn blue — IntelliJ now indexes your generated gRPC classes.

---

### 🔹 Step 3: Build the project

```bash
./gradlew clean build
```

You should see:

```
BUILD SUCCESSFUL
```

---

### 🔹 Step 4: Run the server

```bash
./gradlew bootRun
```

✅ You should see:

```
gRPC Server started, listening on port 9090
```

---

## 🧩 4️⃣ Common Issues and Fixes

| Issue                                | Cause                                   | Fix                                                                            |
| ------------------------------------ | --------------------------------------- | ------------------------------------------------------------------------------ |
| `Cannot resolve symbol 'proto'`      | IntelliJ doesn't see generated folders  | Mark `build/generated/source/proto/main/{java,grpc}` as Generated Sources Root |
| `.java` files not generating         | You didn't run `generateProto` manually | Run `./gradlew generateProto` before build                                     |
| `javax.annotation.Generated` missing | Java 21 removed it                      | Add `implementation 'javax.annotation:javax.annotation-api:1.3.2'`             |
| gRPC folders not appearing in Finder | macOS caching                           | Run `open build/generated/source/proto/main/java`                              |

---

## 🧩 5️⃣ Verify gRPC Code Generation

To test that generation works:

```bash
./gradlew generateProto --info
```

You should see a line like this:

```
--java_out=.../build/generated/source/proto/main/java
--grpc_out=.../build/generated/source/proto/main/grpc
src/main/proto/order.proto
```

That confirms gRPC stubs were created successfully.

---

## 📘 6️⃣ Folder Layout After Successful Build

```
build/
└── generated/
    └── source/
        └── proto/
            └── main/
                ├── java/
                │   ├── com/example/grpc_order_server/proto/CreateOrderRequest.java
                │   └── ...
                └── grpc/
                    └── com/example/grpc_order_server/proto/OrderServiceGrpc.java
```

---

## ✅ Quick Reference Commands

| Task                     | Command                                     |
| ------------------------ | ------------------------------------------- |
| Generate gRPC Java files | `./gradlew generateProto`                   |
| Clean + rebuild          | `./gradlew clean build`                     |
| Run server               | `./gradlew bootRun`                         |
| Check generated files    | `find build/generated/source/proto -type f` |
| Force reload in IntelliJ | "Reload All Gradle Projects"                |

---

## 🚀 Example gRPC Service Implementation

File: `src/main/java/com/example/grpc_order_server/server/OrderServiceImpl.java`

```java
@GrpcService
public class OrderServiceImpl extends OrderServiceGrpc.OrderServiceImplBase {
    private final OrderStore store = OrderStore.getInstance();

    @Override
    public void createOrder(CreateOrderRequest req, StreamObserver<CreateOrderResponse> resObs) {
        String id = store.create(req.getProduct(), req.getQuantity(), req.getCustomer());
        CreateOrderResponse resp = CreateOrderResponse.newBuilder()
                .setOrderId(id).setMessage("Order created").build();
        resObs.onNext(resp);
        resObs.onCompleted();
    }
}
```

---

## 🎯 Summary

✅ Run once:

```bash
./gradlew generateProto
```

✅ Then normal build:

```bash
./gradlew clean build
./gradlew bootRun
```

✅ And mark these folders in IntelliJ as **Generated Sources Root**:

```
build/generated/source/proto/main/java
build/generated/source/proto/main/grpc
```

After that — all `proto` imports and classes (`OrderServiceGrpc`, etc.) will resolve perfectly.

---

**Need help with the client?** Let me know if you'd like a **Client README.md** too for the `grpc-order-client` project to run both end-to-end!