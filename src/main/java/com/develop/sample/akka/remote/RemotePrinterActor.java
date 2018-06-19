package com.develop.sample.akka.remote;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.LoggingAdapter;

public class RemotePrinterActor extends AbstractActor {

    private LoggingAdapter log = getContext().getSystem().log();

    public static class PrinterMessage implements RemoteCreationActor.RemoteMessage {

        private String message;

        public PrinterMessage(String message) {
            this.message = message;
        }

        public void printMessage(LoggingAdapter log, String id) {
            String output = new StringBuilder(message).append(" -- from printer ").append(id).toString();
            log.info(output);
        }

    }

    public String getPrinterId() {
        return this.toString().split("@")[1];
    }

    public static Props createProps() {
        return Props.create(RemotePrinterActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(PrinterMessage.class, msg -> {
                    msg.printMessage(log, getPrinterId());
                    getSender().tell(new RemoteCreationActor.PrinterResponseMessage(getPrinterId()), getSelf());
                })
                .matchAny(unknown -> log.error("Received unknown remote message type {}", unknown))
                .build();
    }

}
