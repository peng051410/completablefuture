package com.nurkiewicz.reactive.stackoverflow;

import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadFromStackOverflowTask implements Callable<String> {

    private static final Logger log = LoggerFactory.getLogger(LoadFromStackOverflowTask.class);

    private final StackOverflowClient client;
    private final String tag;

    public LoadFromStackOverflowTask(StackOverflowClient client, String tag) {
        this.client = client;
        this.tag = tag;
    }

    @Override
    public String call() throws Exception {
        return client.mostRecentQuestionAbout(tag);
    }
}
