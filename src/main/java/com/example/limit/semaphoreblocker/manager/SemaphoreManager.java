package com.example.limit.semaphoreblocker.manager;

import com.example.limit.dto.TransactionRequestDTO;
import com.example.limit.dto.TransactionResponseDTO;
import com.example.limit.dto.Type;
import com.example.limit.semaphoreblocker.model.dto.SemaphoreData;
import com.example.limit.semaphoreblocker.model.property.SemaphoreProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class SemaphoreManager {
    private final Map<Type, SemaphoreData> semaphoreMap;

    public SemaphoreManager(SemaphoreProperty property) {
        this.semaphoreMap = new ConcurrentHashMap<>();

        property
                .getLimits()
                .forEach(
                        l -> semaphoreMap.put(
                                l.getName(), new SemaphoreData()
                                        .setSemaphore(new Semaphore(l.getPermit()))
                                        .setLimit(l)
                        )
                );
    }

    public TransactionResponseDTO execute(TransactionRequestDTO requestDTO, SemaphoreCall<TransactionResponseDTO> call) {
        final var key = requestDTO.getType();

        log.info("Execute semaphore by key: {}", key);

        final var data = semaphoreMap.get(key);

        if (data == null) {
            var message = "Semaphore with key: %s not found".formatted(key);
            log.error("Command over. Cause: {}", message);
            return new TransactionResponseDTO(requestDTO.getCard(), requestDTO.getAmount(), message);
        }

        var semaphore = data.getSemaphore();

        boolean acquired = false;

        try {
            acquired = semaphore.tryAcquire(data.getLimit().getTimeOutMs(), TimeUnit.MILLISECONDS);
            log.info("Try acquire for: {}", semaphore);
            if (!acquired) {
                var message = "Too many %s concurrent requests. Please try again later.".formatted(key);
                log.warn("Semaphore not acquired. Cause: {}", message);
                return new TransactionResponseDTO(requestDTO.getCard(), requestDTO.getAmount(), message);
            }

            return call.execute();

        } catch (InterruptedException e) {
            log.error("Occurred error while using semaphore with message: {}", e.getMessage());
            return new TransactionResponseDTO(requestDTO.getCard(), requestDTO.getAmount(), e.getMessage());
        } finally {
            log.info("Semaphore release...");
            if (acquired) {
                semaphore.release();
            }
        }
    }
}
