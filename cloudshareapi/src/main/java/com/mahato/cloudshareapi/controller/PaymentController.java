package com.mahato.cloudshareapi.controller;


import com.mahato.cloudshareapi.dto.PaymentDTO;
import com.mahato.cloudshareapi.dto.PaymentVerificationDTO;
import com.mahato.cloudshareapi.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody PaymentDTO paymentDTO){

        PaymentDTO response = paymentService.createOrder(paymentDTO);
        if(response.getSuccess()){
            return ResponseEntity.ok(response);
        }else{
            return ResponseEntity.badRequest().body(response);
        }

    }
    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerificationDTO request){
         PaymentDTO response = paymentService.verifyPayment(request);

         if(response.getSuccess()){
             return ResponseEntity.ok(response);
         }else{
             return ResponseEntity.badRequest().body(response);
         }
    }
}
