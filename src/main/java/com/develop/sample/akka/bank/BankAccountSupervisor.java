package com.develop.sample.akka.bank;

import akka.actor.AbstractActor;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class BankAccountSupervisor extends AbstractActor {

    private LoggingAdapter log = getContext().getSystem().log();

    private SupervisorStrategy strategy =
            new OneForOneStrategy(
                    10,
                    Duration.create(1, TimeUnit.MINUTES),
                    DeciderBuilder
                        .match(OverdraftException.class, e -> {
                            log.error("Attempted to withdraw past account limit! Blocking transaction & resuming...");
                            return SupervisorStrategy.resume();
                        })
                        .matchAny(unknown -> SupervisorStrategy.escalate())
                        .build()
            );

    public static Props createProps() {
        return Props.create(BankAccountSupervisor.class);
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    // Our supervisor takes in props and returns a new ActorRef to a created child actor
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Props.class, props -> getSender().tell(getContext().actorOf(props), getSelf()))
                .build();
    }
}
