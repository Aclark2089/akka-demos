package com.develop.sample.akka.streams;

import akka.actor.ActorSystem;
import akka.event.LoggingAdapter;
import akka.http.javadsl.Http;
import akka.http.javadsl.common.EntityStreamingSupport;
import akka.http.javadsl.common.JsonEntityStreamingSupport;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.typesafe.config.ConfigFactory;


public class StreamsApp {

    private static String url = "https://pkgstore.datahub.io/core/population/population_json/data/43d34c2353cbd16a0aa8cadfb193af05/population_json.json";

    public static void main(String[] args) {

        final ActorSystem streamSystem = ActorSystem.create("StreamSystem", ConfigFactory.load("streams"));
        final Materializer mat = ActorMaterializer.create(streamSystem);
        final Http http = Http.get(streamSystem);
        final Unmarshaller<ByteString, PopulationEntity> unmarshaller = Jackson.byteStringUnmarshaller(PopulationEntity.class);
        final JsonEntityStreamingSupport jsonSupport = EntityStreamingSupport.json();
        final LoggingAdapter log = streamSystem.log();

        // Open an http connection to a population dataset
        http.singleRequest(HttpRequest.create(url)).whenComplete((response, t1) -> {

            if (response != null) {

                log.info("Response for request received, streaming data...");

                // Take the given response, extract data, and map the json to PopulationEntity using Jackson unmarshaller
                Source<PopulationEntity, Object> populationSource = response.entity().getDataBytes()
                        .via(jsonSupport.framingDecoder())
                        .mapAsync(5, byteString -> unmarshaller.unmarshal(byteString, mat));

                // Print all mapped population entities from the United States only
                populationSource
                        .filter(entity -> entity.getCountryCode().equals("USA"))
                        .runWith(Sink.foreach(entity -> log.info(entity.toString())), mat);

            }
            else log.error(t1, "Http connection attempt failed!");

        });

    }

}
