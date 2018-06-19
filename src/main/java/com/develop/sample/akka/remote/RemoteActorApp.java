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

    // Load the remote creation system with specifically the 'remotecreation.conf' file settings
    private static void startCreationSystem() {
        creationSystem = ActorSystem.create("RemoteCreationSystem", ConfigFactory.load("remotecreation"));
    }

    // Load the remote creation system with specifically the 'remoteprinter.conf' file settings
    private static void startPrinterSystem() {
        printerSystem = ActorSystem.create("RemotePrinterSystem", ConfigFactory.load("remoteprinter"));
    }


    private static void schedulePrinterMessages() {

        final ActorRef creator = creationSystem.actorOf(RemoteCreationActor.createProps(), "creationActor");
        final Random randomizer = new Random();

        // Send random messages that will be run on multiple ports
        // Could also be set up to run on more than one system with tcp connection
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
