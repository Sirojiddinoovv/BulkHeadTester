package com.example.limit.semaphoreblocker.controller;


import com.example.limit.dto.TransactionRequestDTO;
import com.example.limit.dto.TransactionResponseDTO;
import com.example.limit.semaphoreblocker.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Nodir
 * @date: 12.07.2025
 * @group: Meloman
 **/

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public TransactionResponseDTO pay(@RequestBody TransactionRequestDTO request) {
        return paymentService.pay(request);
    }
}
