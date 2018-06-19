package com.develop.sample.akka;


import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class TestUtils {

    public static Duration TestTimeout = Duration.create(10, TimeUnit.SECONDS);

}
