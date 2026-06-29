# Senior Interview — Talking Points từ CloudOps Task Manager

> Mỗi topic được trình bày theo cấu trúc: **Vấn đề → Quyết định → Lý do → Trade-off → Câu hỏi hay bị hỏi**.  
> Đây là những điểm "ăn tiền" nhất khi kể về project với interviewer senior/principal.

---

## 1. Câu chuyện trung tâm: Tại sao không dùng Spring thuần mà tự build worker system?

> Đây là topic quan trọng nhất. Kể theo đúng hành trình tư duy — từ vấn đề thực tế chạm vào, đến lúc nhận ra Spring không đủ, đến thiết kế, đến trade-off thật sự phải chấp nhận.

---

### Bối cảnh hệ thống

Đây là **DBaaS control plane** — não bộ của hệ thống cung cấp database-as-a-service nội bộ, tương tự Amazon RDS nhưng chạy trên OpenStack riêng. Nó nhận lệnh từ 2 nguồn song song:
- **RabbitMQ**: lệnh điều khiển hạ tầng từ API service (tạo VM, resize, xóa instance...)
- **Socket.IO (TLS)**: event realtime từ agent chạy trên từng VM (status update, auth, heartbeat...)

---

### Điểm khởi đầu — "Dùng Spring @Async cho nhanh"

Ban đầu approach đơn giản nhất là `@Async` + `ThreadPoolTaskExecutor`. Nhận được message → gọi `@Async` method → Spring lo phần còn lại. Không cần viết boilerplate, có Actuator metrics sẵn, dễ test.

Nhưng khi đi vào chi tiết từng loại message, lần lượt gặp phải 2 vấn đề mà Spring không giải quyết được.

---

### Vấn đề 1 — Race condition trên event của cùng một agent

Agent trên VM liên tục gửi event về — ví dụ khi một instance vừa được provision xong, agent gửi liên tiếp:

```
t=0ms:  update_status_instance  → status = BUILDING
t=5ms:  update_status_compute   → status = STARTING
t=10ms: update_status_instance  → status = ACTIVE
```

Với Spring `@Async`, pool chọn thread available bất kỳ cho mỗi event. Kịch bản xảy ra trong thực tế:

```
Thread T1 nhận event [status=BUILDING] lúc t=0ms
Thread T2 nhận event [status=ACTIVE]   lúc t=10ms

T2 xử lý nhanh hơn, write DB: TbInstance.status = ACTIVE  ← đúng
T1 xử lý chậm hơn, write DB: TbInstance.status = BUILDING ← ghi đè, sai

Kết quả: instance mắc kẹt ở BUILDING, user nhìn thấy instance không bao giờ lên ACTIVE.
```

**Giải pháp đầu tiên thử:** `synchronized(agentId.intern())` — lock theo agentId để serialize. Nhưng `String.intern()` là memory leak (String pool không GC được), và dù có lock thì vẫn không đảm bảo thứ tự xử lý — T1 có thể acquire lock sau T2 và vẫn ghi đè.

**Giải pháp thứ hai thử:** DB optimistic locking với `version` field — retry khi conflict. Nhưng bài toán không phải collision, mà là **ordering**. T1 (BUILDING) phải luôn xử lý trước T2 (ACTIVE), dù T2 đến sau. Optimistic locking không giải quyết được ordering.

**Insight then chốt:** Nếu mọi event từ cùng 1 agent **luôn được xử lý bởi cùng 1 thread**, thì ordering tự nhiên được đảm bảo vì thread xử lý tuần tự. Không cần lock, không cần retry, không cần timestamp so sánh.

```java
// Sticky routing: hash agentId → cùng agent luôn vào cùng worker
int index = Math.abs(agentId.hashCode()) % number_app_worker;
MAP_APP_WORKERS.get(index).receiveJob(job);
```

Nhưng để làm được điều này, cần **biết rõ thread nào đang xử lý gì** và **có thể route đến thread cụ thể**. Spring `ThreadPoolTaskExecutor` không expose được điều này — nó chọn thread available bất kỳ, không có khái niệm "gửi task này đến thread số 2". Tại điểm này, rõ ràng cần tự quản lý routing.

**Trade-off của sticky routing:**

Cái được: sequential per agent, không cần lock, đơn giản.

Cái mất:
- **Head-of-line blocking**: một event chậm (vd: DB timeout) của agentA block toàn bộ event sau của agentB nếu cả 2 hash vào cùng bucket. AgentB phải chờ dù worker khác đang rảnh.
- **Uneven load**: agent nào gửi nhiều event nhất sẽ làm overload đúng 1 worker, trong khi worker khác ngồi chờ.
- **Không rebalance được**: để di chuyển agent sang worker khác cần drain queue trước — không thể làm live.

---

### Vấn đề 2 — Worker bị treo không thể replace bằng Spring

`CallbackWorker` gọi HTTP callback ra external service. Khi external service chậm hoặc chết, thread block vô thời hạn. Cần cơ chế watchdog phát hiện và **thay worker mới** mà **không mất các job đang xếp hàng chờ**.

Với Spring `ThreadPoolTaskExecutor`, queue nội bộ được giữ kín bên trong framework — không có API nào để lấy ra hay chuyển sang executor khác. Nếu replace executor, queue cũ cùng toàn bộ job trong đó bị bỏ lại.

Để làm được queue transfer, cần **sở hữu hoàn toàn** `BlockingQueue` — tự tạo, tự giữ reference, tự chuyển khi cần:

```java
// Worker bị treo → tạo worker mới, transfer nguyên queue
newWorker.setJobQueues(worker.getJobQueues()); // transfer reference, không copy
addCallbackWorker(index, newWorker);           // thread mới chạy ngay, xử lý tiếp từ đúng chỗ dừng
```

Một vấn đề thêm: lúc replace, thread cũ vẫn còn sống (đang stuck ở HTTP call). Với `newFixedThreadPool(4)`, pool đầy 4 slot, `execute(newWorker)` vào waiting queue và không bao giờ chạy được vì thread cũ không chịu trả slot. Phải dùng `newCachedThreadPool` — dùng `SynchronousQueue` thay vì `LinkedBlockingQueue`, tạo thread mới ngay lập tức mà không cần slot trống.

---

### Tại sao phải tự build — tóm lại

Spring `@Async` là abstraction — nó ẩn đi thread identity, queue ownership, và routing logic. Đây là điểm mạnh khi không cần quan tâm đến những thứ đó. Nhưng chính sự ẩn đi đó trở thành rào cản khi cần:

1. **Route message đến đúng thread cụ thể** (sticky routing)
2. **Sở hữu và transfer queue** khi cần replace worker
3. **Inject thread mới bất cứ lúc nào** dù pool "đầy"

Cả 3 yêu cầu này đều đòi hỏi **control trực tiếp** lên thread và queue — thứ mà Spring abstracting away. Nếu cố dùng Spring, phải hack vào internal của `ThreadPoolTaskExecutor`, về bản chất vẫn là tự build nhưng trên nền phức tạp hơn.

---

### Thiết kế cuối cùng

```
ApplicationManager.start()
  │
  ├─ Boot 4 loại worker pool (Auth / App / Control / Callback)
  │    └─ mỗi worker: new XxxWorker() → CachedThreadPool.execute(worker)
  │         └─ worker.run(): while(true) { job = queue.take(); process(job); }
  │              ↑ thread block ở đây khi rảnh, không tốn CPU
  │
  ├─ Socket.IO server (Netty, thread riêng)
  ├─ RabbitMQ listener (thread riêng của AMQP client)
  └─ Watchdog timer (HashedWheelTimer, tick mỗi 10 giây)

Job routing:
  Socket.IO auth event  → random AuthAppWorker
  Socket.IO app event   → AppWorker[hash(agentId) % 4]   ← sticky
  RabbitMQ control cmd  → random ControlWorker
  HTTP callback         → random CallbackWorker
```

Worker là **Active Object** — object có thread riêng và queue riêng. Không phải task pool (task pool: nhiều task chia nhau ít thread). Ở đây ngược lại: mỗi worker chiếm đúng 1 thread vĩnh viễn.

---

### Trade-off thật sự so với Spring thuần

| | Custom Worker System | Spring @Async + ThreadPoolTaskExecutor |
|--|---------------------|---------------------------------------|
| **Sticky routing** | Có sẵn | Không có — phải tự implement bên ngoài |
| **Queue ownership & transfer** | Tự sở hữu, transfer được | Queue ẩn trong framework, không transfer được |
| **Dynamic thread replacement** | `CachedThreadPool` — inject thread mới bất cứ lúc nào | `FixedPool` đầy → replacement xếp hàng, không chạy được |
| **Backpressure** | Không có — `LinkedBlockingQueue` unbounded, nguy cơ OOM | Có — `queueCapacity` config, reject khi đầy |
| **Metrics** | Không có — phải tự log thủ công | Spring Actuator expose sẵn: queue size, active threads, completed tasks |
| **Testability** | Khó — static map, không inject mock được | Dễ — Spring DI, swap implementation trong test |
| **Boilerplate** | Nhiều — `WorkerBase`, `WorkerManager`, routing logic | Ít — `@Async` annotation + bean config |
| **Visibility khi debug** | Cao — worker có index, name, queue size rõ ràng | Thấp hơn — task ẩn trong pool internal |

**Đánh đổi lớn nhất phải thừa nhận:** Unbounded `LinkedBlockingQueue`. Nếu RabbitMQ đẩy message nhanh hơn ControlWorker xử lý (vd: OpenStack đang chậm), queue phình to không giới hạn → OOM. Spring với `queueCapacity` hữu hạn sẽ throw `RejectedExecutionException` sớm — dễ alert, dễ monitor hơn nhiều. Đây là điểm nên fix nếu refactor.

---

### Câu hỏi hay bị hỏi

> *"Sao không dùng Kafka hoặc message queue riêng thay vì in-memory BlockingQueue?"*

Kafka phù hợp khi cần durability (message tồn tại dù service restart) và fan-out (nhiều consumer cùng đọc). Ở đây message chỉ cần sống trong 1 process — in-memory queue đơn giản hơn, zero latency, không cần infra thêm. Trade-off: service restart mất toàn bộ job đang trong queue — acceptable vì control plane có thể reconcile lại state từ DB khi khởi động lại.

> *"Sao không dùng reactive (WebFlux/Reactor)?"*

ControlWorker gọi OpenStack4j SDK — synchronous blocking I/O. Wrap vào Reactor chỉ chuyển blocking từ thread này sang scheduler thread của Reactor, không giải quyết được gốc rễ. Để dùng reactive đúng cách cần OpenStack async client — refactor lớn, không đủ ROI ở thời điểm đó.

> *"Nhìn lại, bạn sẽ làm khác đi điểm nào?"*

Giữ nguyên 4 pool độc lập và sticky routing — đây là yêu cầu thật không thể bỏ. Nhưng sẽ fix 3 điểm:
1. `LinkedBlockingQueue(capacity)` — có backpressure, tránh OOM
2. Expose queue size + processing time qua Spring Actuator custom metric — dễ debug production
3. Watchdog cho ControlWorker với threshold cao hơn (vài phút thay vì 5 giây) — hiện tại ControlWorker bị treo không có gì phát hiện

---

## 2. Sticky Routing — Đảm bảo sequential processing per agent

### Vấn đề
Agent gửi nhiều status update liên tiếp. Nếu 2 update của cùng 1 agent xử lý đồng thời trên 2 thread khác nhau → race condition khi cùng write DB record.

### Quyết định
```java
int indexWorker = Math.abs(job.getAccount().getAgentId().hashCode()) % Config.number_app_worker;
AppWorker worker = MAP_APP_WORKERS.get(indexWorker);
worker.receiveJob(job);
```
Hash `agentId` → cùng agent luôn vào cùng 1 worker → xử lý tuần tự tự nhiên, không cần lock.

### Lý do kỹ thuật
Thay vì dùng `synchronized` hay `ConcurrentHashMap.compute()` để serialize per-agent, sticky routing đẩy vấn đề lên tầng routing — đơn giản hơn, không có lock contention, không deadlock.

### Trade-off
Nếu nhiều agent cùng hash vào 1 worker (hash collision), worker đó overload trong khi worker khác rảnh. Tăng `number_app_worker` giảm collision rate nhưng tốn thêm thread.

### Câu hỏi hay bị hỏi
> *"Hash collision có thể dẫn đến starvation không?"*

Có thể. Nếu agent nặng nhất (nhiều event nhất) luôn hash vào Worker0, các agent khác cùng bucket bị chậm. Giải pháp đúng hơn là consistent hashing với virtual nodes, hoặc separate queue per agentId với single dispatcher thread.

---

## 3. Active Object Pattern — Worker không phải thread pool thông thường

### Vấn đề
Phân biệt 2 mô hình thường bị nhầm:

```
Thread Pool thông thường:         Active Object (cái này dùng):
  submit(task) → thread xử lý      receiveJob(job) → enqueue
  thread xong → trả về pool        worker thread luôn chạy, lấy từ queue
  pool reuse thread                 1 worker = 1 thread vĩnh viễn
```

### Quyết định
`WorkerBase` implement `Runnable`, vòng lặp `while(isRunning) { queue.take(); process(); }`. Thread chạy vô tận, không bao giờ trả về pool.

### Lý do kỹ thuật
Model này phù hợp khi cần:
1. **Ordered processing per worker**: queue đảm bảo FIFO
2. **Stateful worker**: worker có thể giữ state giữa các job (connection, cache)
3. **Predictable resource**: biết chính xác bao nhiêu thread đang chạy

`ExecutorService.submit(Runnable)` model ngược lại — thread được allocate per task, phù hợp cho stateless short-lived task.

### Câu hỏi hay bị hỏi
> *"Khác gì Actor Model của Akka?"*

Về conceptual gần giống nhau: mỗi Actor có mailbox (BlockingQueue) và xử lý message tuần tự. Điểm khác: Akka dùng thread sharing (nhiều Actor share ít thread), còn đây mỗi Worker chiếm 1 thread vĩnh viễn — đơn giản hơn nhưng tốn resource hơn.

---

## 4. Saga Pattern — Distributed transaction không cần 2PC

### Vấn đề
Tạo 1 DB instance cần 5 bước gọi 3 hệ thống khác nhau (OpenStack Nova, Neutron, Cinder). Nếu bước 4 fail, cần rollback bước 1–3 đã commit. Không thể dùng ACID transaction vì cross-system.

### Quyết định
```java
builder.saga(saga -> saga
    .startsWith(CheckPrerequisites.class)
        .compensateWith(CheckPrerequisitesUndo.class)    // undo nếu step sau fail
    .then(CreateNetwork.class)
        .compensateWith(CreateNetworkUndo.class)
    .then(CreateVolume.class)
        .compensateWith(CreateVolumeUndo.class)
    ...
)
```

### Lý do kỹ thuật
```
2PC (Two-Phase Commit):                   Saga:
  Coordinator lock tất cả resource          Mỗi step commit local ngay
  → Đợi tất cả ready                       → Không có global lock
  → Commit hoặc abort toàn bộ              → Fail → chạy compensating actions ngược lại
  Vấn đề: coordinator SPOF, lock lâu       Vấn đề: eventual consistency, undo phải tự viết
```

Saga phù hợp hơn cho long-running cross-system workflow vì không giữ lock trong suốt quá trình.

### Trade-off
**Compensation không phải rollback thật:** Nếu `CreateNetwork` thành công và `CreateVolume` fail, `CreateNetworkUndo` gọi OpenStack xóa network. Nhưng nếu OpenStack gặp lỗi lúc xóa? Cần retry logic + manual cleanup — đây là điểm yếu của Saga so với 2PC.

**Không có isolation:** Trong khoảng thời gian giữa các step, resource đã tồn tại một phần. Nếu có request khác đọc DB lúc này có thể thấy trạng thái inconsistent.

### Câu hỏi hay bị hỏi
> *"Saga choreography vs orchestration, project này dùng loại nào?"*

**Orchestration** — có 1 central coordinator (`WorkflowHost` của jWorkflow) điều khiển thứ tự step. Choreography ngược lại: mỗi service tự biết làm gì dựa trên event nhận được, không có trung tâm. Orchestration dễ debug hơn, dễ trace flow hơn, nhưng coordinator là SPOF.

---

## 5. Self-healing Watchdog — Zero job loss khi worker bị treo

### Vấn đề
`CallbackWorker` gọi HTTP ra ngoài. External service không phản hồi → thread block vô thời hạn → jobs mới xếp hàng không ai xử lý.

### Quyết định
```java
// Watchdog mỗi 10 giây:
worker.setRunning(false);
CallbackWorker newWorker = new CallbackWorker();
newWorker.setJobQueues(worker.getJobQueues()); // ← transfer nguyên BlockingQueue
addCallbackWorker(index, newWorker);           // ← thread mới chạy ngay
```

**Trick then chốt:** Transfer reference của `BlockingQueue`, không tạo queue mới. Jobs đang chờ trong queue được tiếp tục xử lý bởi worker mới, không mất 1 job nào.

### Lý do kỹ thuật
```
Nếu tạo queue mới:
  newWorker có queue rỗng → xử lý job mới vào sau
  job4, job5, job6 đang chờ trong queue của worker cũ → không ai lấy → lost

Transfer queue reference:
  newWorker và oldWorker cùng trỏ vào 1 BlockingQueue object
  newWorker.take() lấy job4 → xử lý
  oldWorker đang stuck ở HTTP call → không lấy từ queue → an toàn
  (LinkedBlockingQueue thread-safe → 2 thread cùng trỏ không race condition)
```

### Trade-off
Thread cũ (stuck) vẫn còn sống, tích lũy theo thời gian nếu HTTP call không timeout. Giải pháp: đặt timeout cho HTTP client trong `CallbackWorker`.

Chỉ `CallbackWorker` có watchdog — `ControlWorker` gọi OpenStack (cũng có thể hang) không được cover.

### Câu hỏi hay bị hỏi
> *"Tại sao dùng `newCachedThreadPool` thay vì `newFixedThreadPool`?"*

`newFixedThreadPool(4)` dùng `LinkedBlockingQueue` làm task queue. Khi pool đầy, task mới vào queue, chờ slot trống. Worker thread không bao giờ return (vòng lặp vô tận) → slot không bao giờ trống → replacement thread không bao giờ chạy.

`newCachedThreadPool` dùng `SynchronousQueue` — không buffer, mỗi `execute()` tạo thread mới ngay nếu không có thread rảnh → replacement luôn chạy được dù pool đang "đầy".

---

## 6. Parallel Resize với CompletableFuture + Automatic Rollback

### Vấn đề
Resize một DB cluster có thể có nhiều VM (MySQL replicaset: primary + secondary). Resize tuần tự từng VM mất nhiều thời gian. Nhưng nếu resize song song và 1 VM fail → cả cluster inconsistent (VM có flavor khác nhau).

### Quyết định
```java
// Phase 1: Resize song song tất cả VM
Map<String, CompletableFuture<Boolean>> resizeFutures = new ConcurrentHashMap<>();
for (TbCompute compute : computesToResize) {
    CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(
        () -> resizeFlavor(os, serverId, newFlavorId, TIMEOUT_SECONDS_RESIZE_INSTANCE),
        executor  // max 10 thread
    );
    resizeFutures.put(computeId, future);
}
CompletableFuture.allOf(...).join(); // chờ tất cả xong

// Phase 2: Confirm resize song song
// Phase 3: Nếu có failure → revertAllResizesAsync() cho tất cả đã resize
```

### Lý do kỹ thuật
2 phase riêng biệt vì OpenStack resize có 2 bước: resize (→ VERIFY_RESIZE) rồi confirm (→ ACTIVE). Phải đợi tất cả VM vào VERIFY_RESIZE trước khi confirm bất kỳ cái nào — đảm bảo có thể revert đồng loạt nếu phase 2 fail.

### Trade-off
`ConcurrentHashMap` cho `resizeFutures` đảm bảo thread-safe khi multiple futures cùng put/get. `MAX_THREADS = 10` giới hạn số VM resize đồng thời — tránh quá tải OpenStack API.

Rollback không đảm bảo 100%: nếu revert cũng fail (OpenStack đang có vấn đề) thì cluster vẫn inconsistent. Cần alerting mechanism ở đây.

### Câu hỏi hay bị hỏi
> *"`CompletableFuture.allOf().join()` block thread hiện tại, có vấn đề gì không?"*

Đây đang chạy trong `ControlWorker` thread — vốn đã là long-running blocking thread. Block ở đây là chấp nhận được. Nếu muốn non-blocking hoàn toàn thì dùng `.thenCompose()` chain, nhưng error handling sẽ phức tạp hơn nhiều.

---

## 7. Bill Pugh Singleton — Thread-safe lazy initialization không dùng lock

### Vấn đề
`OSContextManager` cần singleton để tái sử dụng OpenStack authentication session. Vừa phải thread-safe, vừa không muốn lock khi read (read xảy ra mọi lúc).

### Quyết định
```java
public class OSContextManager {
    private static class OSContextManagerHolder {
        private static final OSContextManager INSTANCE = new OSContextManager();
    }
    private OSContextManager() {}
    public static OSContextManager getInstance() {
        return OSContextManagerHolder.INSTANCE; // không cần synchronized
    }
}
```

### Lý do kỹ thuật
JVM chỉ load `OSContextManagerHolder` class khi `getInstance()` được gọi lần đầu (lazy). Class loading trong JVM được đảm bảo thread-safe bởi class loader — không cần `synchronized` hay `volatile`. Đây là lý do tốt hơn double-checked locking.

```
Double-checked locking (hay bị viết sai):
  if (instance == null) {           // không atomic
    synchronized(this) {
      if (instance == null) {
        instance = new Singleton(); // có thể bị reorder bởi JIT
      }
    }
  }
  → Cần volatile để đảm bảo visibility, dễ bug trên Java < 5

Bill Pugh: không có race condition, không có lock, không cần volatile.
```

### Câu hỏi hay bị hỏi
> *"Singleton là anti-pattern, sao vẫn dùng?"*

Singleton bị chỉ trích vì khó test (không inject mock được) và hidden dependency. Trong context này, `OSContextManager` là infrastructure component — lifecycle gắn với JVM, không cần swap implementation. Trade-off chấp nhận được. Nếu muốn testable hơn, có thể dùng Spring `@Bean` singleton scope thay thế.

---

## 8. Strategy + Registry — Mở rộng không cần sửa Worker

### Vấn đề
Hệ thống có 30+ loại command (`create_mysql`, `resize_instance`, `delete_instance`...). Nếu dùng `if-else` trong Worker, mỗi lần thêm command mới phải sửa class Worker — vi phạm Open/Closed Principle.

### Quyết định
```java
// Registry khởi tạo 1 lần, bất biến
static {
    MAP_PROCESSORS.put("create_mysql_standalone", new CreateMysqlStandaloneProcessor());
    MAP_PROCESSORS.put("resize_instance",         new ResizeInstanceProcessor());
    // thêm command mới = thêm 1 dòng ở đây
}

// Worker không biết gì về logic xử lý
ProcessorBase processor = MAP_PROCESSORS.get(job.getServiceId());
processor.process(job);
```

### Lý do kỹ thuật
- **Open/Closed**: thêm command mới → tạo class mới + đăng ký vào map. Worker không thay đổi.
- **Single Responsibility**: mỗi Processor chỉ biết về 1 loại command.
- **Static map**: không cần synchronize vì chỉ write lúc class load, sau đó read-only.

### Trade-off
Processor instances là singleton (dùng chung giữa các job). Phải đảm bảo Processor **stateless** — không lưu state vào field. `ResizeInstanceProcessor` đúng chuẩn này: mọi variable đều là local trong `process()`.

### Câu hỏi hay bị hỏi
> *"Có thể dùng Spring DI thay cho static map không?"*

Được — `@Autowired List<ProcessorBase>` + map theo annotation. Lợi hơn ở chỗ Spring quản lý lifecycle, dễ test hơn (inject mock). Nhưng cần cẩn thận với circular dependency và startup order. Đây là refactor đáng làm nếu codebase grow thêm.

---

## 9. Workflow Context Passing — Chia sẻ data giữa các Step

### Vấn đề
Step 5 (`CreateCompute`) cần biết `glanceImageId` từ Step 1 (`CheckPrerequisites`) và `userData` từ Step 4 (`GenerateCloudInit`). Truyền parameter qua constructor không được vì jWorkflow tự khởi tạo Step instances.

### Quyết định
```java
// Mỗi step đọc/ghi vào shared JSONObject trong workflow context
JSONObject origin = (JSONObject) context.getWorkflow().getData();

// Step 1 ghi:
origin.put(CheckPrerequisitesStandalone.class.getSimpleName(), checkResult);

// Step 5 đọc:
JSONObject checkPrerequisitesData = origin.optJSONObject(
    CheckPrerequisitesStandalone.class.getSimpleName()
);
String glanceImageId = checkPrerequisitesData.optString("glanceImageId");
```

### Lý do kỹ thuật
Dùng class name làm key trong shared JSONObject — tự document, ít collision. Mỗi step viết vào namespace riêng của nó, không ghi đè data của step khác.

### Trade-off
Shared mutable state — nếu step chạy song song (jWorkflow hỗ trợ parallel steps) thì cần synchronize JSONObject access. Hiện tại các step chạy tuần tự nên an toàn.

---

## 10. AES Token Authentication — Không gửi password qua mạng

### Vấn đề
Agent trên VM cần authenticate với TaskManager qua mạng. Không muốn gửi password plaintext hoặc lưu password dạng reversible.

### Quyết định
```java
// DB lưu: agent.encryptedKey (AES key riêng cho mỗi agent)
// Agent gửi: accessToken = AES.encrypt(agentId, encryptedKey)
// Server verify:
String decryptInfo = AESAuth.decrypt(accessToken, agent.getEncryptedKey());
if (decryptInfo.equalsIgnoreCase(agentId)) → authenticated
```

### Lý do kỹ thuật
- Agent không bao giờ gửi `encryptedKey` qua mạng — chỉ gửi token được mã hóa bởi key đó
- Server decrypt được = server biết encryptedKey = agent biết encryptedKey = cùng một bên
- Mỗi agent có key khác nhau — compromise 1 agent không ảnh hưởng agent khác

### Trade-off
AES là symmetric — server và agent cùng biết key. Nếu key bị lộ từ phía server, toàn bộ agent có thể bị impersonate. Giải pháp tốt hơn: asymmetric (agent giữ private key, server giữ public key) — nhưng phức tạp hơn nhiều.

---

## Tổng hợp: Câu hỏi kiểu "Tell me about a challenging technical decision"

Kịch bản kể tốt nhất:

> *"Chúng tôi cần xử lý đồng thời 4 loại workload có SLA hoàn toàn khác nhau trên cùng JVM. Thách thức là job tạo VM có thể mất 10 phút, trong khi job auth agent cần phản hồi trong 50ms. Nếu share thread pool, job nặng sẽ block job nhẹ.*
>
> *Giải pháp là 4 pool worker độc lập với BlockingQueue riêng. Ngoài ra với AppWorker, tôi dùng sticky routing theo hash của agentId — đảm bảo event từ cùng agent luôn xử lý tuần tự mà không cần lock, vì race condition chỉ xảy ra khi 2 thread cùng xử lý event của cùng 1 agent.*
>
> *Một vấn đề khác là CallbackWorker có thể hang vô thời hạn khi HTTP call ra ngoài không phản hồi. Tôi implement watchdog timer phát hiện worker treo và replace bằng worker mới — trick quan trọng là transfer nguyên BlockingQueue sang worker mới thay vì tạo queue mới, đảm bảo không mất job nào đang chờ.*
>
> *Cho phần provision VM, tôi dùng Saga pattern với compensating transaction thay vì 2PC — phù hợp hơn cho long-running cross-system workflow vì không giữ lock trong 5 bước gọi 3 hệ thống OpenStack khác nhau."*

---

## Điểm yếu cần thừa nhận chủ động (shows maturity)

| Điểm yếu | Nên nói gì |
|----------|-----------|
| `LinkedBlockingQueue` unbounded | "Nên đặt capacity để có backpressure, tránh OOM khi producer quá nhanh" |
| Chỉ CallbackWorker có watchdog | "ControlWorker cũng gọi external API, nên có watchdog với threshold cao hơn (phút, không phải giây)" |
| Saga compensation không guaranteed | "Cần retry + dead letter queue cho compensation failures, hiện chưa có" |
| AES symmetric key | "Production-grade nên dùng mutual TLS hoặc asymmetric auth" |
| Không có metrics | "Queue size, processing time chưa expose — khó debug production issues" |
