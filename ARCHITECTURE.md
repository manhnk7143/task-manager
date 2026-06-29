# CloudOps Task Manager — Architecture & Flow

## Mục lục

1. [Tổng quan hệ thống](#1-tổng-quan-hệ-thống)
2. [Kiến trúc tổng thể](#2-kiến-trúc-tổng-thể)
3. [Cấu trúc thư mục](#3-cấu-trúc-thư-mục)
4. [Các thành phần chính](#4-các-thành-phần-chính)
   - 4.1 [Communication Layer](#41-communication-layer)
   - 4.2 [Worker System](#42-worker-system)
     - 4.2.1 [Ý tưởng chia worker — góc nhìn non-tech](#421-ý-tưởng-chia-worker--góc-nhìn-non-tech)
     - 4.2.2 [Ý tưởng chia worker — góc nhìn thuần tech](#422-ý-tưởng-chia-worker--góc-nhìn-thuần-tech)
     - 4.2.3 [Khởi động từ ApplicationManager.start()](#423-khởi-động-từ-applicationmanagerstart)
     - 4.2.4 [Mối quan hệ Worker và Thread](#424-mối-quan-hệ-worker-và-thread)
     - 4.2.5 [Watchdog — CallbackWorker Health Check](#425-watchdog--callbackworker-health-check)
   - 4.3 [Processor Layer](#43-processor-layer)
   - 4.4 [Workflow Engine (Saga Pattern)](#44-workflow-engine-saga-pattern)
   - 4.5 [Manager Layer](#45-manager-layer)
   - 4.6 [Messaging System (RabbitMQ)](#46-messaging-system-rabbitmq)
   - 4.7 [Cache Layer](#47-cache-layer)
   - 4.8 [Database Layer](#48-database-layer)
5. [Data Flow chi tiết](#5-data-flow-chi-tiết)
   - 5.1 [Agent Authentication Flow](#51-agent-authentication-flow)
   - 5.2 [Create DB Instance Flow (ví dụ: MySQL Standalone)](#52-create-db-instance-flow-ví-dụ-mysql-standalone)
   - 5.3 [Resize Instance Flow](#53-resize-instance-flow)
   - 5.4 [Delete Instance Flow](#54-delete-instance-flow)
6. [Workflow Saga Pattern](#6-workflow-saga-pattern)
   - 6.1 [MySQL Standalone](#61-mysql-standalone)
   - 6.2 [MySQL Replicaset](#62-mysql-replicaset)
   - 6.3 [Redis Topologies](#63-redis-topologies)
   - 6.4 [MongoDB Topologies](#64-mongodb-topologies)
   - 6.5 [Kafka Topologies](#65-kafka-topologies)
   - 6.6 [API Gateway Standalone](#66-api-gateway-standalone)
   - 6.7 [Delete Instance](#67-delete-instance)
7. [Database Schema (JPA Entities)](#7-database-schema-jpa-entities)
8. [Service Types (Protocol)](#8-service-types-protocol)
9. [External Services & Integrations](#9-external-services--integrations)
10. [Tech Stack](#10-tech-stack)
11. [Cấu hình hệ thống](#11-cấu-hình-hệ-thống)

---

## 1. Tổng quan hệ thống

**CloudOps Task Manager** là **control plane** (trung tâm điều khiển) của hệ thống DBaaS (Database-as-a-Service) nội bộ. Nó đóng vai trò "não bộ" nhận lệnh từ API service và thực hiện toàn bộ quá trình:

- Provision VM trên OpenStack (Nova, Neutron, Cinder)
- Orchestrate cài đặt database (MySQL, Redis, MongoDB, Kafka, API Gateway)
- Quản lý vòng đời instance (start, stop, restart, resize, delete, backup/restore)
- Giao tiếp 2 chiều với **Agent** chạy trên từng VM qua Socket.IO (TLS)

Hệ thống tương tự **Amazon RDS control plane** nhưng được xây dựng trên nền tảng **OpenStack** riêng, hỗ trợ **2 region**: Hà Nội (HN) và Hồ Chí Minh (HCM).

---

## 2. Kiến trúc tổng thể

```
┌─────────────────────────────────────────────────────────────────┐
│                        External / API Service                    │
└──────────────────────────────┬──────────────────────────────────┘
                               │ RabbitMQ (dbaas.taskmanager)
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                         ClusterManager                           │
│   Lắng nghe RabbitMQ topic → tạo ControlJob → đẩy vào queue    │
└──────────────────────────────┬──────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                         WorkerManager                            │
│  ┌─────────────────┐  ┌──────────────┐  ┌────────────────────┐ │
│  │  AuthAppWorker  │  │  AppWorker   │  │   ControlWorker    │ │
│  │  (auth agent)   │  │ (app events) │  │ (infra operations) │ │
│  └────────┬────────┘  └──────┬───────┘  └─────────┬──────────┘ │
│           │                  │                     │            │
│    ┌──────▼──────┐   ┌───────▼──────┐   ┌─────────▼─────────┐ │
│    │Authentication│  │UpdateStatus  │   │CreateMysql/Redis/ │ │
│    │Processor     │  │Processor     │   │MongoDB/Kafka/...  │ │
│    └─────────────┘  └──────────────┘   └─────────┬─────────┘ │
└─────────────────────────────────────────────────────────────────┘
                                                    │
                                                    ▼
                               ┌────────────────────────────────┐
                               │       WorkflowEngine           │
                               │    (jWorkflow + Saga Pattern)  │
                               │  1. CheckPrerequisites         │
                               │  2. CreateNetwork (OpenStack)  │
                               │  3. CreateVolume  (OpenStack)  │
                               │  4. GenerateCloudInit          │
                               │  5. CreateCompute (OpenStack)  │
                               └──────────────┬─────────────────┘
                                              │
                                              ▼
                               ┌────────────────────────────────┐
                               │   VM on OpenStack Nova         │
                               │   + Agent (Socket.IO TLS) ─────┼──► TaskManager
                               └────────────────────────────────┘
```

---

## 3. Cấu trúc thư mục

```
cloudops-taskmanager/
├── config/
│   ├── app.conf              # Config: RabbitMQ, OpenStack credentials, worker pool sizes
│   ├── hibernate.cfg.xml     # Database connection (MySQL) + ORM settings
│   ├── log.conf              # Log4j configuration
│   └── *.jks / *.p12        # SSL/TLS certificates cho Socket.IO và RabbitMQ
│
├── src/main/java/com/dev/dbaas/
│   ├── main/                 # Entry point: Start.java, Stop.java
│   ├── config/               # Config.java (load app.conf), Constaint.java
│   ├── api/
│   │   ├── controller/       # REST controllers (PingController, SystemController)
│   │   ├── enties/           # Request/Response entities
│   │   └── transaction/      # Transaction runner (CommitCommand, RollbackCommand)
│   ├── listener/             # AppSocketIOListenerTls — Socket.IO server (port 9090/9091)
│   ├── handler/              # AppSocketIoServerHandler — route packet → worker
│   ├── manager/              # Business logic managers (xem mục 4.5)
│   ├── worker/
│   │   ├── AuthAppWorker.java
│   │   ├── AppWorker.java
│   │   ├── ControlWorker.java
│   │   ├── CallbackWorker.java
│   │   ├── ScheduleWorker.java
│   │   ├── job/              # AppJob, ControlJob, CallbackJob, ScheduleJob
│   │   └── processor/        # Processor implementations (xem mục 4.3)
│   ├── workflow/
│   │   ├── flow/             # Workflow definitions (Saga flows)
│   │   └── steps/            # Step implementations + undo steps
│   ├── messaging/            # RabbitMQ integration (MessagingConnection, MessagingSubscriber)
│   ├── database/
│   │   ├── enties/           # JPA entities (Tb* classes)
│   │   ├── dao/              # Data Access Objects
│   │   └── utils/            # DB utility helpers
│   ├── cache/                # CacheRedisCluster, CacheMemcacheCluster
│   ├── entity/               # Domain models (Account, NeutronPort)
│   ├── packet/               # Service type enums (AppServiceType, ControlServiceType)
│   ├── protocol/             # AppPacket, ResponsePacket
│   └── utils/                # AESAuth, NetworkUtil,...
│
├── pom.xml                   # Maven dependencies
└── Dockerfile                # Container build
```

---

## 4. Các thành phần chính

### 4.1 Communication Layer

#### Socket.IO Server (TLS)
- **File:** `listener/AppSocketIOListenerTls.java`
- **Port:** 9090 / 9091 (TLS)
- Agent chạy trên VM **kết nối ngược lại** về đây sau khi được provision
- Mỗi kết nối được handler bởi `AppSocketIoServerHandler` — route packet theo `serviceId` đến đúng worker pool

#### REST API
- **Port:** 8085 (Spring Boot embedded Tomcat, max 8 threads)
- Endpoint `/ping` để health check
- Endpoint `/system` cho system operations

---

### 4.2 Worker System

`WorkerManager` quản lý **4 pool worker** riêng biệt, chạy trên `CachedThreadPool`.

---

#### 4.2.1 Ý tưởng chia worker — góc nhìn non-tech

Hình dung hệ thống như một **bưu điện** có 4 quầy chuyên biệt:

```
┌─────────────────────────────────────────────────────────┐
│                        BƯU ĐIỆN                         │
│                                                         │
│  [Quầy BẢO VỆ]      [Quầy DỊCH VỤ]                    │
│  AuthAppWorker       AppWorker                          │
│  Kiểm tra thẻ        Nhận báo cáo từ                   │
│  ra vào của agent    agent quen mặt                     │
│  Nhanh, không        Mỗi agent có                       │
│  được để chờ         quầy riêng cố định                 │
│                                                         │
│  [Quầy KHO VẬN]     [Quầy GIAO HÀNG]                   │
│  ControlWorker       CallbackWorker                     │
│  Làm việc nặng:      Gửi kết quả ra                    │
│  tạo VM, resize,     ngoài qua HTTP.                   │
│  xóa hạ tầng...      Hay bị chậm vì                    │
│  Chờ lâu là          mạng ngoài không                   │
│  bình thường         ổn định                            │
└─────────────────────────────────────────────────────────┘
```

Mỗi quầy có **N nhân viên** (worker) làm việc song song. Mỗi nhân viên có **hàng đợi riêng** — khách xếp hàng tại quầy, nhân viên lần lượt phục vụ, không quầy nào chen vào việc của quầy khác.

Tại sao tách 4 quầy riêng thay vì 1 quầy làm tất cả?
- Quầy kho vận đang làm việc nặng 5 phút → **không làm chậm** quầy bảo vệ đang cần phản hồi nhanh
- Quầy giao hàng bị treo vì mạng ngoài chậm → **không ảnh hưởng** quầy dịch vụ đang nhận báo cáo từ agent
- Mỗi loại công việc có **đặc thù riêng** → có thể tối ưu độc lập

---

#### 4.2.2 Ý tưởng chia worker — góc nhìn thuần tech

Hệ thống phải xử lý đồng thời 3 loại workload có SLA hoàn toàn khác nhau:

| Loại | Latency kỳ vọng | Nguồn blocking | Nếu chung pool |
|------|----------------|----------------|----------------|
| Auth (AuthAppWorker) | < 50ms | AES decrypt + 1 DB read | Auth bị delay vì ControlJob đang chạy 5 phút |
| App events (AppWorker) | < 100ms | DB write | Bị ảnh hưởng bởi HTTP timeout của Callback |
| Infra ops (ControlWorker) | 1–10 phút | OpenStack API, polling VM status | Expected slow, không được chiếm thread của Auth |
| HTTP callback (CallbackWorker) | Không xác định | External HTTP (mạng ngoài) | Dễ hang vô thời hạn, phải tách hoàn toàn |

**Thread isolation** là giải pháp: mỗi loại workload có `ExecutorService` và `BlockingQueue` riêng → slow job trong ControlWorker không bao giờ block Auth job dù đang dùng chung JVM.

**Sticky routing cho AppWorker** (`agentId.hashCode() % pool_size`) đảm bảo các event từ cùng 1 agent luôn đến cùng 1 worker thread → xử lý tuần tự, tránh race condition khi cùng agent gửi nhiều status update liên tiếp.

---

#### 4.2.3 Khởi động từ `ApplicationManager.start()`

Toàn bộ quá trình khởi động diễn ra theo thứ tự sau:

```
Start.main()
  │
  ├─ Config.loadConfig()          // đọc app.conf: số worker, cổng, credentials
  │
  └─ ApplicationManager.start()
       │
       ├─ [1] Khởi động CallbackWorker pool (số lượng: Config.number_callback_worker)
       │    for i in 0..N:
       │      worker = new CallbackWorker()
       │      worker.setIndex(i), setNameThread("CallbackWorker i"), ...
       │      WorkerManager.addCallbackWorker(i, worker)
       │        → MAP_CALLBACK_WORKER.put(i, worker)
       │        → CALLBACK_WORKER_SERVICE.execute(worker)   ← thread bắt đầu chạy
       │
       ├─ [2] Khởi động AuthAppWorker pool  (tương tự)
       ├─ [3] Khởi động AppWorker pool      (tương tự)
       ├─ [4] Khởi động ControlWorker pool  (tương tự)
       │
       ├─ [5] Khởi động Socket.IO server (TLS)
       │    appSocketIoListenerTls = new AppSocketIOListenerTls(port)
       │    new Thread(appSocketIoListenerTls).start()      ← thread riêng, Netty I/O
       │
       ├─ [6] Kết nối RabbitMQ và lắng nghe topic
       │    ClusterManager.listenTopic(...)                 ← thread riêng của RabbitMQ client
       │
       └─ [7] Khởi động watchdog timer
            WorkerManager.init()                            ← HashedWheelTimer, chạy mỗi 10s
```

Sau bước này, Spring Boot tiếp tục khởi động REST API server (port 8085) — đây chỉ là lớp quản trị, không liên quan đến core processing.

---

#### 4.2.4 Mối quan hệ Worker và Thread

**Worker ≠ Thread.** Worker là một **object Java** (có state: queue, index, name). Thread là **đơn vị thực thi** của OS/JVM. Quan hệ giữa chúng:

```
ApplicationManager.start()
        │
        │  new CallbackWorker()          ← tạo object Worker (chưa có thread)
        │  worker.setIndex(0)
        │  worker.setNameThread("CallbackWorker 0")
        │
        ▼
WorkerManager.addCallbackWorker(0, worker)
        │
        ├─ MAP_CALLBACK_WORKER.put(0, worker)      ← đăng ký vào registry
        │
        └─ CALLBACK_WORKER_SERVICE.execute(worker) ← ExecutorService lấy thread từ pool
                                                      giao cho worker.run()
                                                              │
                                                    ┌─────────▼──────────┐
                                                    │  Worker.run()       │
                                                    │  isRunning = true   │
                                                    │  while (isRunning)  │
                                                    │    job = queue.take() ← BLOCK ở đây
                                                    │    process(job)       ← unblock khi có job
                                                    └────────────────────┘
```

**Thread pool (`CachedThreadPool`) chỉ được dùng một lần lúc startup** để "start" mỗi worker. Sau đó worker chiếm thread đó vĩnh viễn (không bao giờ return), vòng lặp `while(isRunning)` chạy suốt lifetime của ứng dụng.

```
Ký ức sử dụng thread theo thời gian:

[t=0s]  CALLBACK_WORKER_SERVICE.execute(worker0)  → thread T1 chạy worker0.run()
[t=0s]  CALLBACK_WORKER_SERVICE.execute(worker1)  → thread T2 chạy worker1.run()
[t=0s]  CALLBACK_WORKER_SERVICE.execute(worker2)  → thread T3 chạy worker2.run()
[t=0s]  CALLBACK_WORKER_SERVICE.execute(worker3)  → thread T4 chạy worker3.run()

[t=∞]  T1, T2, T3, T4 vẫn đang chạy — block tại queue.take()
       pool không được gọi thêm, trừ khi có worker bị replace
```

**Tại sao dùng `newCachedThreadPool` — giải thích theo 2 cách:**

##### Non-tech: Hình ảnh công ty điều phối nhân sự

Tưởng tượng mỗi Worker là một **nhân viên trực tổng đài** — ngồi chờ điện thoại đổ chuông, bắt máy, xử lý, rồi tiếp tục chờ cuộc gọi tiếp theo. Họ không bao giờ về nhà — ca làm việc kéo dài vô tận.

Bốn loại thread pool tương ứng với 4 mô hình quản lý nhân sự:

```
newFixedThreadPool(4)       → Công ty thuê đúng 4 nhân viên cố định.
                               Nếu 1 người bị liệt (stuck), ghế vẫn của họ.
                               Muốn thuê người thay thế? Không được — biên chế đầy rồi.

newCachedThreadPool         → Công ty ký hợp đồng với công ty phái cử.
                               Cần người → gọi là có ngay, không giới hạn số lượng.
                               Người rảnh quá 60 phút → tự về, không tốn lương.
                               Người bị liệt không chịu về? Vẫn gọi thêm người mới được.

newSingleThreadExecutor     → Chỉ có 1 nhân viên. Mọi cuộc gọi xếp hàng chờ lần lượt.

newScheduledThreadPool      → Nhân viên làm theo lịch: "9h sáng làm việc A", "cứ 10 phút làm B".
```

Kịch bản watchdog trong project:

```
Nhân viên số 2 bị kẹt máy (stuck vì HTTP call không trả lời).
Quản lý (watchdog) phát hiện sau 5 giây → muốn điều người mới vào thay.

Mô hình cố định (FixedThreadPool):
  "Xin lỗi, biên chế 4 người đã đầy. Chờ nhân viên số 2 rời ghế đi đã."
  Nhân viên số 2 không rời → không ai thay thế được → hàng đợi tắc mãi.

Mô hình phái cử (CachedThreadPool):
  Gọi công ty phái cử → nhân viên mới đến ngay, nhận lại toàn bộ hồ sơ đang chờ.
  Nhân viên cũ? Kệ họ, rồi tự rời sau khi cuộc gọi stuck kết thúc.
```

---

##### Thuần tech: `SynchronousQueue` là điểm mấu chốt

**Tại sao dùng `newCachedThreadPool` — so sánh với các loại khác:**

| | `newFixedThreadPool(n)` | `newCachedThreadPool` | `newSingleThreadExecutor` | `newScheduledThreadPool` |
|--|------------------------|----------------------|--------------------------|--------------------------|
| Số thread | Cố định n | Không giới hạn, idle 60s tự die | 1 thread duy nhất | Fixed, hỗ trợ delay/period |
| Queue | `LinkedBlockingQueue` (unbounded) | `SynchronousQueue` (không buffer) | `LinkedBlockingQueue` | `DelayedWorkQueue` |
| Khi pool đầy | Task xếp hàng trong queue | Tạo thread mới ngay | Task xếp hàng | Tạo thread mới (đến max) |
| Phù hợp cho | Task đồng đều, biết trước số lượng | Task ngắn, burst load | Đảm bảo sequential | Cron job, delay task |

**Lý do project này chọn `newCachedThreadPool`:**

Worker là **long-running thread** — một khi đã `execute(worker)`, thread đó không bao giờ return (vòng lặp vô tận). Điều này làm mất đi hầu hết lợi thế của pool thông thường:

```
Với newFixedThreadPool(4):
  Startup: execute(w0), execute(w1), execute(w2), execute(w3) → 4 slot đầy
  Watchdog phát hiện w2 stuck → execute(newW2)
  → Task vào LinkedBlockingQueue, chờ slot trống
  → w2 không bao giờ return (stuck) → slot không bao giờ trống
  → newW2 không bao giờ chạy được  ← health check vô dụng

Với newCachedThreadPool:
  Startup: execute(w0..w3) → 4 thread tạo ra (không có slot limit)
  Watchdog phát hiện w2 stuck → execute(newW2)
  → SynchronousQueue: không buffer, tạo thread mới ngay lập tức
  → newW2 chạy ngay, nhận queue từ w2, tiếp tục xử lý
  → w2 (stuck) tự chết sau khi isRunning=false + timeout
```

**`SynchronousQueue` — điểm quan trọng của `newCachedThreadPool`:**  
Khác với `LinkedBlockingQueue` (có buffer), `SynchronousQueue` không lưu task — mỗi `execute()` phải handoff trực tiếp cho một thread. Nếu không có thread rảnh → tạo thread mới ngay. Đây là lý do replacement worker luôn được cấp thread tức thì, không phải chờ.

**Rủi ro tiềm ẩn:** Nếu watchdog liên tục replace mà thread cũ không chịu chết (stuck ở external call không có timeout), số thread trong pool tăng không kiểm soát. Trong thực tế giảm thiểu bằng cách đặt timeout cho HTTP call trong `CallbackWorker`.

---

#### 4.2.5 Watchdog — CallbackWorker Health Check

Chỉ `CallbackWorker` được giám sát vì đây là worker duy nhất gọi ra external HTTP — nguồn gây hang không thể kiểm soát.

```
WorkerManager.init()  [HashedWheelTimer, mỗi 10 giây]
        │
        ├─ Duyệt MAP_CALLBACK_WORKER
        │    processTime = now - worker.processStartTime
        │    if processTime > 5000ms && processStartTime != 0:
        │        → worker đang STUCK
        │        → thêm vào pendingCallbackWorkers list
        │           (không modify MAP trong forEach → tránh ConcurrentModificationException)
        │
        └─ Với mỗi stuck worker:
             worker.setRunning(false)           ← signal dừng (thread cũ tự chết sau)
             newWorker = new CallbackWorker()
             newWorker.setJobQueues(           ← TRANSFER nguyên BlockingQueue
               worker.getJobQueues())            không mất job nào đang chờ
             addCallbackWorker(index, newWorker) ← thread mới chạy ngay
             │
             └─ init()                          ← tự reschedule, chạy vĩnh viễn
```

Các worker khác (Auth, App, Control) không có watchdog vì:
- **AuthAppWorker / AppWorker**: thao tác nội bộ (AES + DB), latency thấp, ít rủi ro hang
- **ControlWorker**: expected long-running (300s+), tự có timeout logic bên trong từng Processor

---

**Job routing trong WorkerManager:**

```
putAuthAppJob()  → random AuthAppWorker
putAppJob()      → AppWorker[hash(agentId) % pool_size]   ← sticky, tránh race condition
putControlJob()  → random ControlWorker
putCallbackJob() → random CallbackWorker
```

**Concurrency của AppWorker:** với `number_app_worker = 4`, tối đa 4 job chạy đồng thời (mỗi worker 1 job). Sticky routing đảm bảo cùng agent luôn vào cùng worker → sequential per agent, nhưng agent khác hash bucket phải chờ dù worker khác đang rảnh. Tăng pool size → tăng concurrent, giảm hash collision.

---

### 4.3 Processor Layer

Mỗi `serviceId` map đến một `ProcessorBase` implementation. Processor được đăng ký static trong `ControlWorker` và `AppWorker`:

#### App Processors (xử lý event từ agent)

| Service ID | Processor | Mục đích |
|------------|-----------|----------|
| `authentication_agent` | `AuthenticationProcessor` | Xác thực agent bằng AES-encrypted token |
| `update_status_instance` | `UpdateStatusInstanceProcessor` | Cập nhật trạng thái instance |
| `update_status_backup` | `UpdateStatusBackupProcessor` | Cập nhật trạng thái backup |
| `update_status_compute` | `UpdateStatusComputeProcessor` | Cập nhật trạng thái VM |
| `update_status_change_config` | `UpdateStatusChangeConfigProcessor` | Cập nhật trạng thái config change |
| `check_new_version` | `CheckNewVersionProcessor` | Agent kiểm tra firmware version mới |
| `get_agent_info` | `GetAgentInfoProcessor` | Lấy thông tin agent |
| `response_db_action` | `ResponseDbActionProcessor` | Nhận kết quả DB action từ agent |

#### Control Processors (lệnh điều khiển hạ tầng)

| Service ID | Processor | Mục đích |
|------------|-----------|----------|
| `create_mysql_standalone` | `CreateMysqlStandaloneProcessor` | Tạo MySQL single node |
| `create_mysql_replicaset` | `CreateMysqlReplicasetProcessor` | Tạo MySQL Primary-Secondary |
| `create_redis_standalone` | `CreateRedisStandaloneProcessor` | Tạo Redis single node |
| `create_redis_master_slave` | `CreateRedisMasterSlaveProcessor` | Tạo Redis Master-Slave |
| `create_redis_cluster` | `CreateRedisClusterProcessor` | Tạo Redis Cluster (6 node) |
| `create_mongodb_standalone` | `CreateMongodbStandaloneProcessor` | Tạo MongoDB single node |
| `create_mongodb_replicaset` | `CreateMongodbReplicasetProcessor` | Tạo MongoDB ReplicaSet (Primary + Secondary + Arbiter) |
| `create_kafka_single_node` | `CreateKafkaSingleNodeProcessor` | Tạo Kafka single node |
| `create_kafka_cluster` | `CreateKafkaClusterProcessor` | Tạo Kafka Cluster |
| `create_api_gateway_standalone` | `CreateApiGatewayStandaloneProcessor` | Tạo API Gateway |
| `start_instance` | `StartInstanceProcessor` | Khởi động instance |
| `stop_instance` | `StopInstanceProcessor` | Dừng instance |
| `restart_instance` | `RestartInstanceProcessor` | Restart instance |
| `resize_instance` | `ResizeInstanceProcessor` | Thay đổi flavor (CPU/RAM) — parallel resize |
| `resize_volume` | `ResizeVolumeProcessor` | Mở rộng storage volume |
| `delete_instance` | `DeleteInstanceProcessor` | Xóa instance + cleanup hạ tầng |
| `create_backup` | `CreateBackupProcessor` | Tạo backup |
| `restore_backup` | `RestoreBackupProcessor` | Khôi phục từ backup |
| `change_group_config` | `ChangeGroupConfigProcessor` | Thay đổi cấu hình DB group |
| `set_password` | `SetPasswordInstanceProcessor` | Đổi mật khẩu DB |
| `promote_slave_master` | `PromoteSlaveMasterProcessor` | Promote Slave lên Master |
| `db_action` | `DbActionProcessor` | Thực thi lệnh DB trực tiếp |
| `attach_security_group` | `AttachSecurityGroupProcessor` | Gắn security group |
| `detach_security_group` | `DetachSecurityGroupProcessor` | Gỡ security group |
| `update_monitor_service` | `UpdateMonitorServiceProcessor` | Cập nhật monitoring |
| `test_command` | `TestCommandProcessor` | Kiểm tra kết nối lệnh |

---

### 4.4 Workflow Engine (Saga Pattern)

Sử dụng thư viện **jWorkflow** để orchestrate các tác vụ phức tạp có nhiều bước. Mỗi bước đều có **compensating transaction (undo)** để rollback khi có lỗi — đây là **Saga Pattern**.

**Nguyên lý hoạt động:**
```
Step 1 ──success──► Step 2 ──success──► Step 3 ──FAIL──► Undo Step 2 ──► Undo Step 1
         ◄─undo ──           ◄─ undo ──
```

Mỗi flow định nghĩa thứ tự step và undo tương ứng:
```java
builder.saga(saga -> saga
    .startsWith(CheckPrerequisites.class)
        .compensateWith(CheckPrerequisitesUndo.class)
    .then(CreateNetwork.class)
        .compensateWith(CreateNetworkUndo.class)
    .then(CreateVolume.class)
        .compensateWith(CreateVolumeUndo.class)
    .then(GenerateCloudInit.class)
        .compensateWith(GenerateCloudInitUndo.class)
    .then(CreateCompute.class)
        .compensateWith(CreateComputeUndo.class)
)
```

---

### 4.5 Manager Layer

Các manager là singleton, quản lý business logic cụ thể:

| Manager | Trách nhiệm |
|---------|-------------|
| `WorkerManager` | Quản lý 4 worker pool, routing job, health check |
| `ClusterManager` | Bridge RabbitMQ → ControlJob |
| `AgentManager` | Track các agent đang kết nối, quản lý Account session |
| `SessionManager` | Map socketId ↔ agentId |
| `InstanceManager` | CRUD database instance (TbInstance) |
| `ComputeManager` | CRUD VM (TbCompute), gọi OpenStack Nova |
| `NetworkManager` | Quản lý network (TbNetwork), gọi OpenStack Neutron |
| `VolumeManager` | Quản lý volume (TbVolume), gọi OpenStack Cinder |
| `WorkFlowManager` | Khởi động workflow engine cho từng loại operation |
| `OSContextManager` | Quản lý OpenStack client per region (HN/HCM) |
| `ZoneManager` | Quản lý availability zones |
| `FlavorManager` | Quản lý VM flavor (CPU/RAM templates) |
| `DatastoreManager` | Quản lý datastore types |
| `DatastoreVersionManager` | Quản lý các phiên bản DB được hỗ trợ |
| `DatastoreModeManager` | Quản lý topology modes |
| `DatastoreConfigurationManager` | Quản lý default config cho từng DB type |
| `GroupConfigurationManager` | Quản lý config group của instance |
| `ConfigurationManager` | System-wide configuration |
| `BackupManager` | Quản lý backup records |
| `BackupStrategyManager` | Quản lý backup policy (schedule, retention) |
| `AgentHeartbeatManager` | Theo dõi heartbeat từ agent |
| `AgentFirmwareManager` | Quản lý phiên bản firmware của agent |
| `CacheManager` | Interface cho Redis Cluster và Memcache |
| `DbConnectionManager` | Quản lý Hibernate SessionFactory |
| `ChannelSocketIoAttachmentManager` | Quản lý Socket.IO channel attachments |

---

### 4.6 Messaging System (RabbitMQ)

**MessagingConnection** sử dụng **SSL/TLS** (certificate-based auth):

```
RabbitMQ Server: redis.api-connect.io:5674

Topics:
  - dbaas.taskmanager    ← nhận lệnh điều khiển từ API service
  - dbaas.api            ← gửi kết quả về API service
  - dbaas.resource_instance ← cập nhật resource state
```

**Flow:**
1. API service publish message vào `dbaas.taskmanager`
2. `ClusterManager.listenTopic()` nhận message
3. Parse JSON → tạo `ControlJob` với `serviceId` + `data`
4. Đẩy vào `WorkerManager.putControlJob()`

**Gửi kết quả:**
```java
ClusterManager.sendTopic(rabbitConnection, port, username, password, exchange, queue, data)
```

---

### 4.7 Cache Layer

Hỗ trợ 2 backend qua interface `ICacheClient`:

| Backend | Class | Dùng khi |
|---------|-------|----------|
| **Redis Cluster** | `CacheRedisCluster` | Production cluster, password-protected |
| **Memcache Cluster** | `CacheMemcacheCluster` | Alternative caching |

`CacheManager` là facade, routing đến đúng backend dựa theo config.

---

### 4.8 Database Layer

**Database:** MySQL 8.0 (`cloudops_dbaas`) tại `203.205.9.195:3306`

**ORM:** Hibernate 5.3.6 + HikariCP connection pooling

**DAO Pattern:** `GenericDao<T>` là base class, các DAO cụ thể extend và thêm query riêng:
```
GenericDao<T>
  ├── AgentDao
  ├── InstanceDao
  ├── ComputeDao
  ├── VolumeDao
  └── ...
```

**Utility layer:** `AgentUtil`, `InstanceUtil`, `ComputeUtil`,... là static helper wrap DAO calls.

---

## 5. Data Flow chi tiết

### 5.1 Agent Authentication Flow

```
Agent (VM) ─── Socket.IO TLS ──► AppSocketIOListenerTls (port 9090)
                                          │
                                          ▼
                              AppSocketIoServerHandler.handlePacket()
                                          │ serviceId = "authentication_agent"
                                          ▼
                              WorkerManager.putAuthAppJob(appJob)
                                          │ random worker
                                          ▼
                              AuthAppWorker → AuthenticationProcessor.process()
                                          │
                                ┌─────────┴─────────┐
                                │                   │
                         Decode packet         AgentManager.findById(agentId)
                                │                   │
                         AESAuth.decrypt(     TbAgent from DB
                           accessToken,             │
                           agent.encryptedKey)      │
                                │                   │
                                └─────────┬─────────┘
                                          │ decrypted == agentId ?
                                    ┌─────┴──────┐
                                   YES           NO
                                    │             │
                          SessionManager    ResponsePacket(result=-4)
                          .addSession()           │
                                    │         Socket.IO response
                          Account cached
                          in AgentManager
                                    │
                          ResponsePacket(result=1)
                                    │
                          Socket.IO response ──► Agent
```

**Token validation:** `AESAuth.decrypt(accessToken, agent.encryptedKey)` phải trả về đúng `agentId`. Đây là cơ chế xác thực không cần password gửi qua mạng.

---

### 5.2 Create DB Instance Flow (ví dụ: MySQL Standalone)

```
API Service
    │
    │  JSON: { serviceId: "create_mysql_standalone",
    │           data: { instanceId, flavor, region, network... } }
    │
    ▼ RabbitMQ (dbaas.taskmanager)
ClusterManager.onProcess()
    │
    │  new ControlJob(serviceId, data)
    ▼
WorkerManager.putControlJob()
    │ random ControlWorker
    ▼
ControlWorker.process(job)
    │ MAP_PROCESSORS.get("create_mysql_standalone")
    ▼
CreateMysqlStandaloneProcessor.process()
    │
    │  job.decodePacket() → JSONObject input
    ▼
WorkFlowManager.startCreateMysqlStandalone(input)
    │
    ▼ jWorkflow engine
CreateMysqlStandaloneFlow (Saga)
    │
    ├─ Step 1: CheckPrerequisitesStandalone
    │    - Validate inputs (flavor, network, region tồn tại)
    │    - Kiểm tra quota OpenStack
    │    - Lưu TbInstance vào DB với status=BUILDING
    │
    ├─ Step 2: CreateNetworkStandalone
    │    - Gọi OpenStack Neutron: tạo Network + Subnet + Port
    │    - Lưu TbNetwork vào DB
    │
    ├─ Step 3: CreateVolumeStandalone
    │    - Gọi OpenStack Cinder: tạo Block Storage volume
    │    - Lưu TbVolume vào DB
    │
    ├─ Step 4: GenerateCloudInitStandalone
    │    - Sinh cloud-init script (cài MySQL, config replication,...)
    │    - Sinh password ngẫu nhiên, lưu encrypted vào DB
    │
    └─ Step 5: CreateComputeStandalone
         - Gọi OpenStack Nova: boot VM với image + flavor + network + volume + cloud-init
         - Lưu TbCompute + TbAgent vào DB
         - TbInstance.status = ACTIVE (sau khi agent connect)

            ↓ (vài phút sau, VM boot xong)

Agent tự động cài đặt trên VM
    │
    │  Socket.IO TLS
    ▼
TaskManager ← Agent.connect()
    │
    ▼ authentication_agent
SessionManager.addSession()
    │
    ▼ update_status_instance
UpdateStatusInstanceProcessor
    │
    │  TbInstance.status = ACTIVE
    ▼
(Optional) gửi callback về API service qua RabbitMQ
```

**Rollback nếu Step 3 (CreateVolume) fail:**
```
CreateVolumeStandaloneUndo  → xóa volume (nếu đã tạo)
CreateNetworkStandaloneUndo → xóa network
CheckPrerequisitesUndo      → TbInstance.status = ERROR, cleanup DB records
```

---

### 5.3 Resize Instance Flow

```
API Service → RabbitMQ → ControlJob(serviceId="resize_instance")
    │
    ▼
ResizeInstanceProcessor.process()
    │
    ├─ Validate: instance exists, not DELETING
    ├─ Get OSClient cho đúng region
    ├─ Validate newFlavor tồn tại
    ├─ Collect danh sách TbCompute cần resize
    │
    ├─ [PARALLEL - CompletableFuture per compute]
    │   ├─ resizeFlavor(os, serverId, newFlavorId, timeout=300s)
    │   │    - Gọi os.compute().servers().resize()
    │   │    - Poll mỗi 5s đến khi status = VERIFY_RESIZE (hoặc timeout)
    │   └─ ...
    │
    ├─ Nếu tất cả resize thành công:
    │   ├─ [PARALLEL] confirmResize() cho từng compute
    │   │    - Gọi os.compute().servers().confirmResize()
    │   │    - Poll đến khi status = ACTIVE
    │   └─ Update TbCompute.flavorId + TbInstance.flavorId trong DB
    │
    └─ Nếu có lỗi:
        └─ revertAllResizesAsync() → gọi os.compute().servers().revertResize()
           cho tất cả compute đã resize thành công (đảm bảo consistency)
```

**Đặc điểm:** Sử dụng `CompletableFuture` để resize song song tối đa 10 VM cùng lúc (`MAX_THREADS = 10`). Nếu 1 VM fail, rollback tất cả.

---

### 5.4 Delete Instance Flow

```
DeleteInstanceProcessor → DeleteInstanceFlow (Saga)
    │
    ├─ Step 1: CleanDatabaseInstance    → soft-delete TbInstance, TbCompute, TbAgent, TbNetwork, TbVolume
    ├─ Step 2: DeleteComputeNova        → gọi OpenStack Nova: xóa VM
    ├─ Step 3: DeleteNetworkNeutron     → gọi OpenStack Neutron: xóa network, subnet, port
    └─ Step 4: DeleteVolumeCinder       → gọi OpenStack Cinder: xóa volume
```

---

## 6. Workflow Saga Pattern

### 6.1 MySQL Standalone

```
CheckPrerequisitesStandalone → CreateNetworkStandalone → CreateVolumeStandalone
    → GenerateCloudInitStandalone → CreateComputeStandalone
```

Mỗi bước có `*Undo` tương ứng.

---

### 6.2 MySQL Replicaset

```
CheckPrerequisites → CreateNetwork → CreateVolume → GenerateCloudInit → CreateCompute
```

Tương tự standalone nhưng provision **nhiều node** (Primary + Secondary). Cloud-init script cấu hình replication tự động.

---

### 6.3 Redis Topologies

| Topology | Steps | Nodes |
|----------|-------|-------|
| **Standalone** | CheckPrerequisites → CreateNetwork → CreateVolume → GenerateCloudInit → CreateCompute | 1 |
| **Master-Slave** | CheckPrerequisites → CreateNetwork → CreateVolume → GenerateCloudInit → CreateCompute | 2+ (1 master, N slaves) |
| **Cluster** | CheckPrerequisites + LoadConfigGroupCluster → CreateNetwork → CreateVolume → GenerateCloudInit → CreateCompute | 6 (3 master + 3 replica) |

Redis Cluster có thêm bước `LoadConfigGroupCluster` để cấu hình cluster topology.

---

### 6.4 MongoDB Topologies

| Topology | Steps |
|----------|-------|
| **Standalone** | CheckPrerequisitesStandalone → LoadConfigGroupStandalone → CreateNetwork → CreateVolume → GenerateCloudInit → CreateCompute |
| **ReplicaSet** | CheckPrerequisitesReplicaset → LoadConfigGroupReplicaset → Primary(Network+Volume+CloudInit+Compute) → Secondary(Network+Volume+CloudInit+Compute) → Arbiter(Network+CloudInit+Compute) |

MongoDB ReplicaSet provision **3 loại node riêng biệt**:
- **Primary:** Có volume (data node)
- **Secondary:** Có volume (data node)
- **Arbiter:** Không có volume (chỉ vote, không lưu data)

---

### 6.5 Kafka Topologies

| Topology | Steps |
|----------|-------|
| **Single Node** | CheckPrerequisitesSingleNode → CreateNetworkSingleNode → CreateVolumeSingleNode → GenerateCloudInitSingleNode → CreateComputeSingleNode |
| **Cluster** | CheckPrerequisitesCluster → CreateNetworkCluster → CreateVolumeCluster → GenerateCloudInitCluster → CreateComputeCluster |

---

### 6.6 API Gateway Standalone

```
CheckPrerequisites → CreateNetwork → CreateVolume → GenerateCloudInit → CreateCompute
```

---

### 6.7 Delete Instance

```
CleanDatabaseInstance → DeleteComputeNova → DeleteNetworkNeutron → DeleteVolumeCinder
```

Xóa theo thứ tự: DB records trước (soft-delete), sau đó xóa hạ tầng OpenStack.

---

## 7. Database Schema (JPA Entities)

### Core Entities

```
TbInstance
  ├─ id (UUID)
  ├─ name
  ├─ status          (BUILDING, ACTIVE, ERROR, DELETING, STOPPED,...)
  ├─ datastoreId     → TbDatastore
  ├─ datastoreVersionId → TbDatastoreVersion
  ├─ datastoreModeId → TbDatastoreMode
  ├─ flavorId        → OpenStack flavor ID
  ├─ regionId        (HN/HCM)
  ├─ orgId, projectId
  ├─ createdAt, updatedAt, deletedAt (soft-delete)
  └─ ...

TbCompute
  ├─ id (UUID)
  ├─ instanceId      → TbInstance
  ├─ novaInstanceId  → OpenStack Nova server ID
  ├─ flavorId
  ├─ status
  ├─ ipAddress
  └─ ...

TbAgent
  ├─ id (UUID)
  ├─ computeId       → TbCompute
  ├─ instanceId      → TbInstance
  ├─ encryptedKey    (dùng cho AES auth)
  ├─ agentVersion
  ├─ agentFirmwareId → TbAgentFirmware
  ├─ orgId, projectId
  └─ ...

TbNetwork
  ├─ id (UUID)
  ├─ instanceId      → TbInstance
  ├─ neutronNetworkId → OpenStack Neutron network ID
  ├─ subnetId
  └─ ...

TbVolume
  ├─ id (UUID)
  ├─ computeId       → TbCompute
  ├─ cinderVolumeId  → OpenStack Cinder volume ID
  ├─ sizeGb
  └─ ...
```

### Datastore Entities

```
TbDatastore          (MySQL, Redis, MongoDB, Kafka, APIGateway)
TbDatastoreVersion   (mysql-8.0, redis-6.2,...)
TbDatastoreMode      (standalone, replicaset, master_slave, cluster)
TbDatastoreConfiguration  (default config params per datastore type)
TbGroupConfiguration      (runtime config của instance)
TbConfiguration           (system-wide config)
```

### Operational Entities

```
TbBackup             (backup records)
TbBackupStrategy     (schedule, retention policy)
TbAgentHeartbeat     (liveness monitoring)
TbAgentFirmware      (agent binary versions)
```

---

## 8. Service Types (Protocol)

### AppServiceType (agent → taskmanager)

```java
AUTHENTICATION          = "authentication_agent"
UPDATE_STATUS_INSTANCE  = "update_status_instance"
UPDATE_STATUS_BACKUP    = "update_status_backup"
UPDATE_STATUS_COMPUTE   = "update_status_compute"
CHECK_NEW_VERSION       = "check_new_version"
GET_AGENT_INFO          = "get_agent_info"
RESPONSE_DB_ACTION      = "response_db_action"
UPDATE_STATUS_CHANGE_CONFIG = "update_status_change_config"
```

### ControlServiceType (api → taskmanager qua RabbitMQ)

```java
// Create DB instances
CREATE_MYSQL_STANDALONE     = "create_mysql_standalone"
CREATE_MYSQL_REPLICASET     = "create_mysql_replicaset"
CREATE_REDIS_STANDALONE     = "create_redis_standalone"
CREATE_REDIS_MASTER_SLAVE   = "create_redis_master_slave"
CREATE_REDIS_CLUSTER        = "create_redis_cluster"
CREATE_MONGODB_STANDALONE   = "create_mongodb_standalone"
CREATE_MONGODB_REPLICASET   = "create_mongodb_replicaset"
CREATE_KAFKA_SINGLE_NODE    = "create_kafka_single_node"
CREATE_KAFKA_CLUSTER        = "create_kafka_cluster"
CREATE_API_GATEWAY_STANDALONE = "create_api_gateway_standalone"

// Lifecycle
START_INSTANCE          = "start_instance"
STOP_INSTANCE           = "stop_instance"
RESTART_INSTANCE        = "restart_instance"
DELETE_INSTANCE         = "delete_instance"
RESIZE_INSTANCE         = "resize_instance"
RESIZE_VOLUME           = "resize_volume"

// Operations
CREATE_BACKUP           = "create_backup"
RESTORE_BACKUP          = "restore_backup"
CHANGE_GROUP_CONFIG     = "change_group_config"
SET_PASSWORD            = "set_password"
PROMOTE_SLAVE_MASTER    = "promote_slave_master"
DB_ACTION               = "db_action"
ATTACH_SECURITY_GROUP   = "attach_security_group"
DETACH_SECURITY_GROUP   = "detach_security_group"
UPDATE_MONITOR_SERVICE  = "update_monitor_service"
TEST_COMMAND            = "test_command"
```

---

## 9. External Services & Integrations

### OpenStack (Dual-Region)

| Region | Services dùng |
|--------|--------------|
| **HN (Hà Nội)** | Nova (compute), Neutron (network), Cinder (block storage) |
| **HCM (Hồ Chí Minh)** | Nova (compute), Neutron (network), Cinder (block storage) |

**Client:** OpenStack4j SDK (`org.openstack4j:openstack4j:3.11`)

`OSContextManager` quản lý authenticated client per region, tái sử dụng session để tránh overhead auth.

### RabbitMQ

```
Host:     redis.api-connect.io:5674
Security: TLS (mutual auth với certificate)
Topics:
  dbaas.taskmanager      (inbound: lệnh từ API)
  dbaas.api              (outbound: kết quả về API)
  dbaas.resource_instance (outbound: resource state updates)
```

### MySQL Database

```
Host:     203.205.9.195:3306
Database: cloudops_dbaas
ORM:      Hibernate 5.3.6 + HikariCP connection pool
```

### Cache

```
Redis Cluster:  Password-protected, configurable nodes
Memcache:       Alternative backend
```

### Socket.IO (Agent Connection)

```
Port:     9090, 9091
Protocol: Socket.IO over TLS (netty-socketio 2.0.3)
Auth:     AES-256 encrypted access token
Direction: Agent → TaskManager (agent initiate connection)
```

---

## 10. Tech Stack

| Thành phần | Technology | Version |
|-----------|-----------|---------|
| Framework | Spring Boot | 2.1.7 |
| Language | Java | 9 |
| ORM | Hibernate | 5.3.6 |
| Real-time | netty-socketio | 2.0.3 |
| Messaging | RabbitMQ | 5.x client |
| Workflow | jWorkflow | 1.0 |
| Cloud SDK | OpenStack4j | 3.11 |
| Cache | Jedis (Redis), Memcached | - |
| Serialization | org.json, Gson, Jackson | - |
| Build | Maven | - |
| Container | Docker | - |
| Logging | Log4j | - |

---

## 11. Cấu hình hệ thống

### Worker Pool Sizes (app.conf)

```properties
number_auth_app_worker = 4
number_app_worker      = 4
number_control_worker  = 4
number_callback_worker = 4
```

Tăng giá trị này khi cần xử lý đồng thời nhiều hơn. Với `AppWorker`, tăng pool size sẽ tăng parallelism nhưng sticky routing vẫn đảm bảo agent events không bị race.

### Timeout cấu hình (ResizeInstanceProcessor)

```java
TIMEOUT_SECONDS_RESIZE_INSTANCE  = 300  // 5 phút chờ VM resize
TIMEOUT_SECONDS_CONFIRM_RESIZE   = 180  // 3 phút chờ confirm
POLLING_INTERVAL_SECONDS         = 5    // poll OpenStack mỗi 5 giây
MAX_THREADS                      = 10   // tối đa 10 VM resize song song
```

### Spring Boot

```yaml
server.port: 8085
server.tomcat.max-threads: 8   # REST API threads (Socket.IO dùng Netty riêng)
```