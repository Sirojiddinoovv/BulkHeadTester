package com.example.limit.semaphoreblocker.service;


import com.example.limit.dto.TransactionRequestDTO;
import com.example.limit.dto.TransactionResponseDTO;
import com.example.limit.semaphoreblocker.manager.SemaphoreManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author: Nodir
 * @date: 12.07.2025
 * @group: Meloman
 **/

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {
    private final DebitService debitService;
    private final SemaphoreManager semaphoreManager;

    public TransactionResponseDTO pay(TransactionRequestDTO requestDTO) {
        log.info("Received command to debit payment with requestDTO dto: {}", requestDTO);

        var result = semaphoreManager.execute(requestDTO, () -> debitService.debit(requestDTO));

        log.info("Finished command to debit payment with responseDTO: {}", result);
        return result;
    }
}
