package com.develop.sample.akka.bank;

import java.util.Random;

// A basic bank account model!
// We should never overdraft our account or else we will have to pay the fee
public class BankAccount {

    private double balance;
    private static final Random randomizer = new Random();

    public BankAccount() {
        balance = 0.0;
    }

    public BankAccount(double startingBalance) {
        balance = startingBalance;
    }

    public double checkBalance() {
        return balance;
    }

    public double deposit(double amount) {
        return balance += amount;
    }

    public double withdraw(double amount) {
        if (isWithdrawingPastZero(amount)) throw new OverdraftException();
        lag();
        return balance -= amount;
    }

    private boolean isWithdrawingPastZero(double amount) {
        return (balance - amount) < 0;
    }

    // Simulate selective lag where our threads could cross over
    // 50% of the time, it works every time
    private void lag() {
        if (randomizer.nextBoolean()) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {}
        }
    }

}
