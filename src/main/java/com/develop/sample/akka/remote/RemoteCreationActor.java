package com.develop.sample.akka.remote;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.LoggingAdapter;

import java.io.Serializable;

public class RemoteCreationActor extends AbstractActor {

    private LoggingAdapter log = getContext().getSystem().log();

    public interface RemoteMessage extends Serializable {}

    public static class CreatePrinterMessage implements RemoteMessage {}

    public static class PrinterResponseMessage implements RemoteMessage {
        private String printerId;

        public PrinterResponseMessage(String id) {
            this.printerId = id;
        }

        public String getPrinterId() {
            return printerId;
        }
    }

    public static Props createProps() {
        return Props.create(RemoteCreationActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreatePrinterMessage.class, msg -> {
                    log.info("Creating new remote message printer!");
                    ActorRef printerActor = getContext().actorOf(RemotePrinterActor.createProps());
                    printerActor.tell(new RemotePrinterActor.PrinterMessage("Ran this on a different system"), getSelf());
                })
                .match(PrinterResponseMessage.class, msg -> {
                    log.info("Printer {} responded on this systems port", msg.getPrinterId());
                    getContext().stop(getSender());
                })
                .matchAny(unknown -> log.error("Received unknown message {}", unknown))
                .build();
    }


}
