package com.develop.sample.akka.bank;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.LoggingAdapter;

public class BankAccountActor extends AbstractActor  {

    private LoggingAdapter log = getContext().getSystem().log();

    /**
     * It is good practice to include all messages tied with one actor as members of that actor
     */
    public interface Request {}

    public static abstract class TransactionRequest implements Request {
        private double amount;
        public TransactionRequest(double amount) {
            this.amount = amount;
        }
        public double getAmount() {
            return amount;
        }
    }

    public static class WithdrawRequest extends TransactionRequest {
        public WithdrawRequest(double amount) {
            super(amount);
        }
    }

    public static class DepositRequest extends TransactionRequest {
        public DepositRequest(double amount) {
            super(amount);
        }
    }

    public static class CheckBalanceRequest implements Request {}

    private BankAccount account = new BankAccount();

    public static Props createProps() {
        return Props.create(BankAccountActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(DepositRequest.class, request -> {
                    log.info("Depositing {} to account", request.getAmount());
                    account.deposit(request.getAmount());
                    log.info("New balance after deposit is {}", account.checkBalance());
                })
                .match(WithdrawRequest.class, request -> {
                    log.info("Withdrawing {} from account", request.getAmount());
                    account.withdraw(request.getAmount());
                    log.info("New balance after withdraw is {}", account.checkBalance());
                })
                .match(CheckBalanceRequest.class, request -> getSender().tell(account.checkBalance(), getSelf()))
                .matchAny(unknown -> log.error("Unknown message {} sent to BankAccountActor", unknown))
                .build();
    }

}
