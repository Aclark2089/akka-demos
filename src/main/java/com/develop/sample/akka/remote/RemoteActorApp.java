package com.develop.sample.akka.remote;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.ConfigFactory;
import scala.concurrent.duration.Duration;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class RemoteActorApp {

    private static ActorSystem creationSystem;
    private static ActorSystem printerSystem;

    public static void main(String[] args) {
        startCreationSystem();
        startPrinterSystem();
        schedulePrinterMessages();
    }

    private static void startCreationSystem() {
        creationSystem = ActorSystem.create("RemoteCreationSystem", ConfigFactory.load("remotecreation"));
    }

    private static void startPrinterSystem() {
        printerSystem = ActorSystem.create("RemotePrinterSystem", ConfigFactory.load("remoteprinter"));
    }

    private static void schedulePrinterMessages() {

        final ActorRef creator = creationSystem.actorOf(RemoteCreationActor.createProps(), "creationActor");
        final Random randomizer = new Random();

        creationSystem.scheduler()
                .schedule(
                        Duration.create(1, TimeUnit.SECONDS),   // Delay before start
                        Duration.create(3, TimeUnit.SECONDS),   // Interval between msgs
                        () -> {
                            // Randomly send a new creation response
                            if (randomizer.nextInt(2) % 2 == 0) {
                                creator.tell(new RemoteCreationActor.CreatePrinterMessage(), ActorRef.noSender());
                            }
                        },
                        creationSystem.dispatcher()
                );

    }

}
