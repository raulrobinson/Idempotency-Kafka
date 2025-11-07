package com.example.api.controller;

import com.example.api.dto.TransactionRequest;
import com.example.api.dto.TransactionResponse;
import com.example.api.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService service;
    public TransactionController(TransactionService service){ this.service = service; }

    @PostMapping
    public Mono<ResponseEntity<TransactionResponse>> create(@RequestHeader(name="Idempotency-Key", required=false) String idem,
                                                            @RequestBody TransactionRequest req){
        String key = (idem==null||idem.isBlank()) ? UUID.randomUUID().toString() : idem;
        return service.accept(key, req).map(body -> ResponseEntity.accepted().body(body));
    }

    @GetMapping("/{idemKey}")
    public Mono<ResponseEntity<TransactionResponse>> get(@PathVariable String idemKey){
        return service.accept(idemKey, new TransactionRequest(null, null))
                .map(ResponseEntity::ok);
    }
}