# BulkHeadTester

Демонстрационный сервис на Spring Boot, иллюстрирующий два подхода к ограничению параллельных вызовов и их fallback-обработке:

1. **Resilience4j Bulkhead** с аннотацией `@Bulkhead`  
2. **Custom SemaphoreManager** с семафорами по типам транзакций  

---

## 📦 Структура проекта

```
bulkhead-tester/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/example/limit/
│   │   │   ├── BulkHeadTesterApplication.java
│   │   │   ├── dto/
│   │   │   │   ├── TransactionRequestDTO.java
│   │   │   │   ├── TransactionResponseDTO.java
│   │   │   │   └── Type.java
│   │   │   ├── bulkheadtester/               ← Resilience4j Bulkhead
│   │   │   │   ├── service/TransactionService.java
│   │   │   │   └── controller/TransactionController.java
│   │   │   └── semaphoreblocker/             ← Custom SemaphoreManager
│   │   │       ├── service/PaymentService.java
│   │   │       ├── service/DebitService.java
│   │   │       ├── manager/SemaphoreManager.java
│   │   │       ├── manager/SemaphoreCall.java
│   │   │       └── model/
│   │   │           ├── property/SemaphoreProperty.java
│   │   │           └── dto/SemaphoreData.java
│   │   └── resources/
│   │       └── application.yml
└── README.md
```

---

## 1. Resilience4j Bulkhead

### Конфигурация в `application.yml`
```yaml
server:
  port: 8588

spring:
  application:
    name: BulkHeadTester

resilience4j:
  bulkhead:
    instances:
      bulkhead:
        maxConcurrentCalls: 2      # макс. одновременных вызовов
        maxWaitDuration: 5s        # время ожидания до fallback
internal:
  semaphore:
    limits:
      - name: MASTERCARD           # название типа
        permit: 2                  # сколько запросов допускается в течении времени
        time-out-ms: 2_000         # время тайм оута
      - name: VISA
        permit: 1
        time-out-ms: 60_000
      - name: ONLINE
        permit: 4
        time-out-ms: 30_000
```

### Код

- **`@Bulkhead`**  
  ```java
  @Service
  public class TransactionService {
      private final Random random = new Random();

      @Bulkhead(name = "bulkhead", 
                type = Bulkhead.Type.SEMAPHORE, 
                fallbackMethod = "debitFallback")
      public TransactionResponseDTO debit(TransactionRequestDTO requestDTO) {
          boolean ok = random.nextInt(2) == 0;
          String desc = ok ? "SUCCESS" : "FAILED";
          return new TransactionResponseDTO(requestDTO.getCard(),
                                            requestDTO.getAmount(),
                                            desc);
      }

      public TransactionResponseDTO debitFallback(TransactionRequestDTO req, Throwable ex) {
          // ex.getMessage() будет содержать текст ошибки Bulkhead или другое сообщение
          return new TransactionResponseDTO(req.getCard(),
                                            req.getAmount(),
                                            ex.getMessage());
      }
  }
  ```

- **Контроллер**  
  ```java
  @RestController
  @RequestMapping("/transactions")
  public class TransactionController {
      private final TransactionService service;

      @PostMapping("/debit")
      public TransactionResponseDTO debit(@RequestBody TransactionRequestDTO req) {
          return service.debit(req);
      }
  }
  ```

### Возможные ответы

1. **Успех**  
   ```json
   {
     "card": "1234-5678-9012-3456",
     "amount": 100.0,
     "description": "SUCCESS"
   }
   ```
2. **Логический `FAILED` (внутри метода)**  
   ```json
   {
     "card": "1234-5678-9012-3456",
     "amount": 100.0,
     "description": "FAILED"
   }
   ```
3. **Превышение Bulkhead** (fallback)  
   ```json
   {
     "card": "1234-5678-9012-3456",
     "amount": 100.0,
     "description": "Bulkhead 'bulkhead' is full and does not permit further calls"
   }
   ```

---

## 2. Custom SemaphoreManager

### Конфигурация в `application.yml`
```yaml
server:
  port: 8588

spring:
  application:
    name: BulkHeadTester

internal:
  semaphore:
    limits:
      - name: MASTERCARD
        permit: 2
        time-out-ms: 2000
      - name: VISA
        permit: 1
        time-out-ms: 60000
      - name: ONLINE
        permit: 4
        time-out-ms: 30000
```

### Код

- **`SemaphoreManager`**  
  ```java
  @Component
  @ConfigurationProperties(prefix = "internal.semaphore")
  public class SemaphoreProperty { … } 

  @Component
  public class SemaphoreManager {
      // инициализирует Map<Type, Semaphore> из настроек
      public TransactionResponseDTO execute(TransactionRequestDTO req, SemaphoreCall<TransactionResponseDTO> call) {
          Semaphore sem = …; int timeout = …;
          boolean acquired = sem.tryAcquire(timeout, TimeUnit.MILLISECONDS);
          if (!acquired) {
              return new TransactionResponseDTO(
                  req.getCard(), req.getAmount(),
                  "Too many " + req.getType() +
                  " concurrent requests. Please try again later."
              );
          }
          try {
              return call.execute();
          } finally {
              sem.release();
          }
      }
  }
  ```

- **`DebitService`**  
  ```java
  @Service
  public class DebitService {
      private final Random random = new Random();

      @SneakyThrows
      public TransactionResponseDTO debit(TransactionRequestDTO req) {
          int value = random.nextInt(2);
          Thread.sleep(value * 1000L);
          String desc = (value % 2 == 0) ? "SUCCESS" : "FAILED";
          return new TransactionResponseDTO(req.getCard(), req.getAmount(), desc);
      }
  }
  ```

- **`PaymentService` + контроллер**  
  ```java
  @Service
  public class PaymentService {
      public TransactionResponseDTO pay(TransactionRequestDTO req) {
          return semaphoreManager.execute(req, () -> debitService.debit(req));
      }
  }

  @RestController
  @RequestMapping("/payment")
  public class PaymentController {
      @PostMapping
      public TransactionResponseDTO pay(@RequestBody TransactionRequestDTO req) {
          return paymentService.pay(req);
      }
  }
  ```

### Возможные ответы

1. **Успех**  
   ```json
   {
     "card": "1234-5678-9012-3456",
     "amount": 100.0,
     "description": "SUCCESS"
   }
   ```
2. **Логический `FAILED`**  
   ```json
   {
     "card": "1234-5678-9012-3456",
     "amount": 100.0,
     "description": "FAILED"
   }
   ```
3. **Превышение семафора**  
   ```json
   {
     "card": "1234-5678-9012-3456",
     "amount": 100.0,
     "description": "Too many VISA concurrent requests. Please try again later."
   }
   ```

---

**MIT © Ваше Имя**
