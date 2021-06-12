package com.jx.test.simplewallet.controller;

import com.jx.test.simplewallet.exception.InvalidPaymentException;
import com.jx.test.simplewallet.service.WalletService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.management.InvalidAttributeValueException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping("/init")
    public String init(@RequestParam(required = true) int[] coins) {
        try {
            walletService.init(coins);

            return "Success";
        } catch (InvalidAttributeValueException e) {
            return "Invalid request: " + e.getMessage();
        }
    }

    @GetMapping("/check")
    public String check() {
        var latest = walletService.latest();
        var leftCoins = IntStream.of(latest).mapToObj(Integer::toString).collect(Collectors.joining(", "));

        return "My current coins are [" + leftCoins + "]";
    }

    @GetMapping("/pay")
    public String pay(@RequestParam(required = true) int amount) {
        try {
            var left = walletService.pay(amount);
            var leftCoins = IntStream.of(left).mapToObj(Integer::toString).collect(Collectors.joining(", "));

            return "Successfully paid " + amount + "\r\n" + "My current coins are [" + leftCoins + "]";
        } catch (InvalidAttributeValueException e) {
            return "Invalid payment: " + e.getMessage();
        } catch (InvalidPaymentException e) {
            var leftCoins = IntStream
                .of(e.getLeftCoins())
                .mapToObj(Integer::toString)
                .collect(Collectors.joining(", "));

            return (
                "You do not have sufficient coins to pay " +
                amount +
                ".\r\n" +
                "My current coins are [" +
                leftCoins +
                "]"
            );
        }
    }
}
