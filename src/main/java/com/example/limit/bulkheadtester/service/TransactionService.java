package com.example.limit.bulkheadtester.service;

import java.util.Random;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.example.limit.dto.TransactionRequestDTO;
import com.example.limit.dto.TransactionResponseDTO;

@Service
@Slf4j
public class TransactionService {
    private final Random random = new Random();

    @SneakyThrows
    @Bulkhead(name = "bulkhead", fallbackMethod = "debitFallback", type = Bulkhead.Type.SEMAPHORE)
    public TransactionResponseDTO debit(TransactionRequestDTO requestDTO) {
        log.info("Received command to debit with requestDTO dto: {}", requestDTO);
        int value = random.nextInt(5,20);

        //some business logic instead of Thread sleep
        log.info("Random value is: {}", value);
        Thread.sleep(value * 1000L);
        TransactionResponseDTO responseDTO;
        if (value % 2 == 0) {
            responseDTO = new TransactionResponseDTO(requestDTO.getCard(), requestDTO.getAmount(), "SUCCESS");
        } else {
            responseDTO = new TransactionResponseDTO(requestDTO.getCard(), requestDTO.getAmount(), "FAILED");
        }

        log.info("Finished command to debit with responseDTO: {}", responseDTO);
        return responseDTO;
    }

    public TransactionResponseDTO debitFallback(TransactionRequestDTO requestDTO, Throwable ex) {
        log.error("Debit fallback exception with message: {}", ex.getMessage());
        return new TransactionResponseDTO(requestDTO.getCard(), requestDTO.getAmount(), ex.getMessage());
    }
}
