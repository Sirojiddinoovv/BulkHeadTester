package com.example.limit.bulkheadtester.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.example.limit.dto.TransactionRequestDTO;
import com.example.limit.dto.TransactionResponseDTO;
import com.example.limit.bulkheadtester.service.TransactionService;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;

    @PostMapping("/debit")
    public TransactionResponseDTO debit(@RequestBody TransactionRequestDTO request) {
        return service.debit(request);
    }
}
