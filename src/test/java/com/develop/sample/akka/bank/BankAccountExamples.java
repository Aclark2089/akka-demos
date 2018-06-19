package com.develop.sample.akka.bank;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import org.junit.*;
import scala.concurrent.Await;

import java.util.stream.IntStream;

import static akka.pattern.Patterns.ask;
import static com.develop.sample.akka.TestUtils.TestTimeout;

public class BankAccountExamples {

    private static ActorSystem testSystem;
    private static ActorRef supervisor;

    private ActorRef bankActor;

    @BeforeClass
    public static void startUp() {
        testSystem = ActorSystem.create("BankAccountTestSystem");
        supervisor = testSystem.actorOf(BankAccountSupervisor.createProps(), "BankAccountSupervisorActor");
    }

    @AfterClass
    public static void shutdown() {
        testSystem.terminate();
    }

    @Before
    public void setUp() throws Exception {
        bankActor = (ActorRef) Await.result(ask(supervisor, BankAccountActor.createProps(), 5000), TestTimeout);
    }

    @Ignore
    @Test
    public void givenMultithreading_whenMultipleRequestsOccur_thenAttemptToAvoidOverdraftingAccount() throws Exception {
        BankAccount account = new BankAccount();

        // A thread that makes a bunch of async withdraw requests
        // Larger amount and in greater number than deposit requests, highly likely to overwhelm and overdraft our account!
        Thread withdrawThread = new Thread(() -> {
            IntStream.range(0, 25).forEach(i -> new Thread(() -> {
                try {
                    account.withdraw(8);
                } catch (Exception e) {} // Quietly ignore the OverdraftExceptions
            }).start());
        });

        // A thread that makes a bunch of async deposit requests
        Thread depositThread = new Thread(() -> {
            IntStream.range(0, 20).forEach(i -> new Thread(() -> {
                account.deposit(5);
            }).start());
        });

        depositThread.start();
        withdrawThread.start();

        // Wait for the threads to finish
        Thread.sleep(2000L);

        // We cannot make any assumptions about our bank account's state, this test fails most of the time!
        // The account balance may have had its state violated, could be a negative value and voided the overdraft protection!
        // Though we are checking the amount to prevent this, it doesn't matter since it is accessed across multiple threads. Yuck.
        // We could put a lock or rewrite the method / balance to be 'synchronized' across threads, but that's a large performance loss
        System.out.println("Final account balance: " + account.checkBalance());
        assert account.checkBalance() >= 0;

    }

    @Test
    public void givenSupervisedActor_whenMultipleRequestsReceived_thenAccountShouldNotBeOverdrafted() throws Exception {

        // A thread that asynchronously sends withdraw requests to the BankAccountActor
        Thread actorWithdrawThread = new Thread(() -> {
            IntStream.range(0, 25).forEach(i -> {
                new Thread(() -> bankActor.tell(new BankAccountActor.WithdrawRequest(7), ActorRef.noSender())).start();
            });
        });

        // A thread that asynchronously sends deposit requests to the BankAccountActor
        Thread actorDepositThread = new Thread(() -> {
            IntStream.range(0, 20).forEach(i ->
                    new Thread(() -> bankActor.tell(new BankAccountActor.DepositRequest(5), ActorRef.noSender())).start());
        });

        actorDepositThread.start();
        actorWithdrawThread.start();

        Thread.sleep(2000L);

        // Ask our actor to respond with a result when it is ready
        Double result = (Double) Await.result(ask(bankActor, new BankAccountActor.CheckBalanceRequest(), 5000), TestTimeout);

        // We never overdrafted our account!
        // The actor processed each of our messages sent to it while preserving our state! Bad withdraws were dropped in this example
        System.out.println("Final account balance: " + result);
        assert result >= 0;

    }
}
