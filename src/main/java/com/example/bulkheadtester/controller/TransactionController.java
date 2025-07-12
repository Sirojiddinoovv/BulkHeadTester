package com.example.bulkheadtester.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.example.bulkheadtester.dto.TransactionRequestDTO;
import com.example.bulkheadtester.dto.TransactionResponseDTO;
import com.example.bulkheadtester.service.TransactionService;

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
