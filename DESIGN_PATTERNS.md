# Design Patterns trong CloudOps Task Manager

> Tài liệu ôn tập phỏng vấn — mỗi pattern gồm: định nghĩa, nơi áp dụng trong code, và điểm cần nhớ.

---

## 1. Strategy Pattern

**Định nghĩa:** Định nghĩa một họ thuật toán, đóng gói từng cái, và cho phép hoán đổi chúng lúc runtime.

**Trong code:**
```
ProcessorBase<T>                    ← interface (Strategy)
  ├── CreateMysqlStandaloneProcessor
  ├── ResizeInstanceProcessor
  ├── AuthenticationProcessor
  └── ... (30+ implementations)

ControlWorker:
  MAP_PROCESSORS.get(job.getServiceId()) → chọn đúng processor lúc runtime
```

**Điểm cần nhớ:** Thay vì một `if-else` khổng lồ theo `serviceId`, mỗi hành vi được đóng gói riêng trong 1 class. Thêm service mới = thêm class mới + đăng ký vào map, không sửa `ControlWorker`.

---

## 2. Template Method Pattern

**Định nghĩa:** Định nghĩa skeleton của thuật toán trong base class, để subclass override các bước cụ thể mà không thay đổi cấu trúc tổng thể.

**Trong code:**
```java
// WorkerBase.java — định nghĩa skeleton
public void run() {
    while (isRunning) {
        T job = jobQueues.take();       // cố định
        process(job);                   // ← subclass override
    }
}

protected abstract void process(T job);         // AuthAppWorker, AppWorker, ControlWorker tự implement
protected abstract void process(List<T> jobs);
```

**Điểm cần nhớ:** `WorkerBase` kiểm soát toàn bộ vòng lặp, blocking queue, health tracking. Subclass chỉ quan tâm đến logic xử lý job — không cần biết gì về thread hay queue.

---

## 3. Command Pattern

**Định nghĩa:** Đóng gói một request thành object, cho phép tham số hóa, queue, và undo.

**Trong code:**
```
JobBase
  ├── AppJob      { AppPacket packet, SocketIOClient client }
  ├── ControlJob  { String serviceId, String data, long time }
  └── CallbackJob { String callbackUrl, String data }
```

**Điểm cần nhớ:** Socket.IO handler và RabbitMQ listener không gọi processor trực tiếp — chúng tạo Job object rồi đẩy vào queue. Điều này **tách sender khỏi receiver** hoàn toàn: handler không biết ai xử lý, processor không biết job đến từ đâu.

---

## 4. Observer / Publish-Subscribe Pattern

**Định nghĩa:** Khi object thay đổi trạng thái, tất cả dependents được notify tự động.

**Trong code — 2 nơi:**

```java
// 1. RabbitMQ: MessagingSubscriber implements java.util.concurrent.Flow.Subscriber
public abstract class MessagingSubscriber implements Flow.Subscriber<Message> {
    public abstract void onProcess(Message message); // subclass implement
}

// ClusterManager đăng ký subscriber:
MessagingConnection.listenQueue(..., messagingSubscriber, ...)

// 2. Socket.IO: AppSocketIOListenerTls đăng ký event listener
server.addEventListener("message", ..., (client, data, ack) -> { ... })
```

**Điểm cần nhớ:** Hệ thống dùng Java 9 `Flow` API (reactive streams standard) cho RabbitMQ — không phải Observer tự viết. `onNext()` gọi `onProcess()` → `subscription.request(1)` để kéo message tiếp (backpressure mechanism).

---

## 5. Singleton Pattern (Bill Pugh variant)

**Định nghĩa:** Đảm bảo class chỉ có một instance duy nhất trong toàn bộ ứng dụng.

**Trong code:**
```java
// OSContextManager.java — Bill Pugh Singleton (thread-safe, lazy init)
public class OSContextManager {
    private static class OSContextManagerHolder {
        private static final OSContextManager INSTANCE = new OSContextManager();
    }
    private OSContextManager() {}
    public static OSContextManager getInstance() {
        return OSContextManagerHolder.INSTANCE;
    }
}
```

**Điểm cần nhớ:** Bill Pugh dùng static inner class — JVM chỉ load `OSContextManagerHolder` khi `getInstance()` được gọi lần đầu (lazy), và class loading trong JVM là thread-safe nên không cần `synchronized`. Tốt hơn double-checked locking ở chỗ đơn giản hơn và không có vấn đề với memory model cũ.

Các manager khác (`WorkerManager`, `ClusterManager`) dùng static methods/fields, thực chất cũng là singleton nhưng không theo pattern chuẩn.

---

## 6. Facade Pattern

**Định nghĩa:** Cung cấp interface đơn giản cho một hệ thống con phức tạp.

**Trong code:**
```java
// OSContextManager — facade cho toàn bộ OpenStack SDK
OSContextManager.getInstance().createNovaServer(...)   // ẩn: auth, region, builder pattern, retry
OSContextManager.getInstance().createVolume(...)        // ẩn: Cinder API details
OSContextManager.getInstance().createPort(...)          // ẩn: Neutron API, IPv4 extraction

// InstanceManager — facade cho DB + cache operations
InstanceManager.findById(instanceId)    // ẩn: Hibernate session, DAO, connection pool
InstanceManager.update(instance)        // ẩn: transaction, commit, rollback
```

**Điểm cần nhớ:** Processor chỉ gọi `OSContextManager.getInstance().createVolume(...)` — không biết gì về OpenStack4j SDK, authentication flow, hay region routing. Đây là lý do tách `OSContextManager` thành class riêng thay vì viết thẳng vào Processor.

---

## 7. DAO Pattern (Data Access Object)

**Định nghĩa:** Tách logic truy cập data ra khỏi business logic.

**Trong code:**
```
GenericDao<T>           ← base class: findOne, create, update, delete với Hibernate session
  ├── AgentDao          ← thêm: findByComputeId, findByInstanceId
  ├── InstanceDao       ← thêm: findByStatus, findByOrgId
  ├── ComputeDao
  └── ...

Utility layer (static helper):
  AgentUtil.findById()   → wrap AgentDao + DbConnectionManager.getSession()
  InstanceUtil.update()  → wrap InstanceDao + session management
```

**Điểm cần nhớ:** Có 2 layer — DAO (nhận Session từ ngoài vào) và Util (tự lấy Session). Processor gọi Util layer, không gọi DAO trực tiếp. `GenericDao` dùng generics để tránh lặp code CRUD.

---

## 8. Builder Pattern

**Định nghĩa:** Tách construction của object phức tạp ra khỏi representation, cho phép tạo nhiều representation khác nhau.

**Trong code — 2 nơi:**

```java
// 1. jWorkflow — build saga flow
builder.saga(saga -> saga
    .startsWith(CheckPrerequisites.class)
        .compensateWith(CheckPrerequisitesUndo.class)
    .then(CreateNetwork.class)
        .compensateWith(CreateNetworkUndo.class)
    ...
)

// 2. OpenStack4j — build server/volume/port/security group
Builders.server()
    .name(serverName)
    .image(glanceImageId)
    .flavor(flavorId)
    .userData(userData)
    .blockDevice(Builders.blockDeviceMapping()...build())
    .build()
```

**Điểm cần nhớ:** Builder tốt khi object có nhiều optional field và thứ tự set không quan trọng. `WorkflowBuilder` còn dùng fluent interface (method chaining) để đọc như config thay vì code.

---

## 9. Saga Pattern

**Định nghĩa:** Quản lý distributed transaction bằng cách chia thành nhiều local transaction, mỗi transaction có compensating transaction để rollback nếu fail.

**Trong code:**
```
CreateMysqlStandaloneFlow:
  Step 1: CheckPrerequisites    ←→  Undo: CheckPrerequisitesUndo
  Step 2: CreateNetwork         ←→  Undo: CreateNetworkUndo
  Step 3: CreateVolume          ←→  Undo: CreateVolumeUndo
  Step 4: GenerateCloudInit     ←→  Undo: GenerateCloudInitUndo
  Step 5: CreateCompute         ←→  Undo: CreateComputeUndo

Nếu Step 3 fail:
  → tự động chạy: CreateNetworkUndo → CheckPrerequisitesUndo
```

**Điểm cần nhớn:** Saga khác Transaction truyền thống ở chỗ không có global lock — mỗi step commit ngay. Khi fail, phải chạy compensating actions (không phải rollback DB). Phù hợp cho distributed system vì không cần 2PC. Trade-off: eventual consistency, và compensating logic phải tự viết.

---

## 10. Producer-Consumer Pattern

**Định nghĩa:** Tách producer (tạo data) và consumer (xử lý data) bằng một shared buffer, cho phép chúng chạy ở tốc độ khác nhau.

**Trong code:**
```
Producer:
  Socket.IO handler    → WorkerManager.putAppJob(job)
  RabbitMQ listener   → WorkerManager.putControlJob(job)
              ↓
  job.add(queue)       ← LinkedBlockingQueue (shared buffer)
              ↓
Consumer:
  WorkerBase.run()     → job = queue.take()  → process(job)
```

**Điểm cần nhớ:** `LinkedBlockingQueue` là thread-safe, `take()` block nếu queue rỗng (không busy-wait, không tốn CPU). Producer không bao giờ chờ consumer — đẩy vào queue rồi return ngay. Điểm yếu hiện tại: queue unbounded → nên dùng `LinkedBlockingQueue(capacity)` để có backpressure.

---

## 11. Active Object Pattern

**Định nghĩa:** Tách method execution khỏi method invocation bằng cách mỗi object có thread và message queue riêng.

**Trong code:**
```
WorkerBase:
  - Có thread riêng (chạy trong ExecutorService)
  - Có BlockingQueue riêng
  - receiveJob(job) = enqueue (invocation, từ bất kỳ thread nào)
  - run() loop = dequeue + execute (execution, luôn trên worker thread)
```

**Điểm cần nhớ:** Active Object là nền tảng của Actor Model (Akka, Erlang). Ở đây implement thủ công. Lợi ích: worker thread không bao giờ bị race condition vì chỉ có 1 thread xử lý queue của nó — đây chính là lý do sticky routing cho AppWorker đảm bảo sequential processing per agent.

---

## 12. Registry Pattern

**Định nghĩa:** Object registry lưu trữ và cung cấp access đến các object/service theo key.

**Trong code — 2 loại:**

```java
// 1. Processor registry — map serviceId → processor (static, bất biến)
static {
    MAP_PROCESSORS.put("create_mysql_standalone", new CreateMysqlStandaloneProcessor());
    MAP_PROCESSORS.put("resize_instance",         new ResizeInstanceProcessor());
    // ...
}
ProcessorBase processor = MAP_PROCESSORS.get(job.getServiceId());

// 2. Worker registry — map index → worker instance (dynamic, thay đổi khi replace)
MAP_CALLBACK_WORKER.put(index, worker);
MAP_APP_WORKERS.put(index, worker);
```

**Điểm cần nhớ:** Processor registry là static — đăng ký 1 lần, không thay đổi, không cần synchronize. Worker registry là dynamic — watchdog thay thế worker lúc runtime, dùng `ConcurrentHashMap` để thread-safe.

---

## 13. Watchdog Pattern

**Định nghĩa:** Background monitor định kỳ kiểm tra sức khỏe của component và tự động khôi phục khi phát hiện sự cố.

**Trong code:**
```java
// WorkerManager.init() — chạy mỗi 10 giây bằng HashedWheelTimer
if (processTime > 5000 && processStartTime != 0) {
    // worker bị treo
    worker.setRunning(false);
    CallbackWorker newWorker = new CallbackWorker();
    newWorker.setJobQueues(worker.getJobQueues()); // transfer queue, không mất job
    addCallbackWorker(index, newWorker);
}
init(); // self-reschedule
```

**Điểm cần nhớ:** Dùng `HashedWheelTimer` (Netty) thay vì `ScheduledExecutorService` — hiệu quả hơn cho nhiều timer với độ chính xác thấp (ms range). Self-reschedule ở cuối thay vì `scheduleAtFixedRate` để tránh overlap nếu check chạy lâu hơn interval.

---

## Tóm tắt theo nhóm GoF

| Nhóm | Pattern | Class chính |
|------|---------|-------------|
| **Creational** | Singleton (Bill Pugh) | `OSContextManager` |
| **Creational** | Builder | `WorkflowBuilder`, `Builders.*` (OpenStack4j) |
| **Structural** | Facade | `OSContextManager`, `InstanceManager`, `ComputeManager` |
| **Structural** | DAO | `GenericDao<T>`, `AgentDao`, `InstanceDao`... |
| **Behavioral** | Strategy | `ProcessorBase<T>` + 30 implementations |
| **Behavioral** | Template Method | `WorkerBase<T>.run()` |
| **Behavioral** | Command | `AppJob`, `ControlJob`, `CallbackJob` |
| **Behavioral** | Observer | `MessagingSubscriber`, Socket.IO listeners |

## Patterns ngoài GoF

| Pattern | Class chính |
|---------|-------------|
| Saga | `CreateMysql/Redis/MongodbFlow` + `*Undo` steps |
| Producer-Consumer | `WorkerBase` + `LinkedBlockingQueue` |
| Active Object | `WorkerBase` (thread + queue per object) |
| Registry | `MAP_PROCESSORS`, `MAP_CALLBACK_WORKER` |
| Watchdog | `WorkerManager.init()` |
