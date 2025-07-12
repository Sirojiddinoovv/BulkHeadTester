package com.example.limit.semaphoreblocker.service;


import com.example.limit.dto.TransactionRequestDTO;
import com.example.limit.dto.TransactionResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * @author: Nodir
 * @date: 12.07.2025
 * @group: Meloman
 **/

@Service
@Slf4j
@RequiredArgsConstructor
public class DebitService {
    private final Random random = new Random();

    @SneakyThrows
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
}
