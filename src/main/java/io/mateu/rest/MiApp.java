package io.mateu.rest;

import io.mateu.util.CORSFilter;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("rest")
public class MiApp extends ResourceConfig {
    public MiApp() {
        packages("org.foo.rest;io.mateu.mdd.rest;io.mateu.rest");

        register(new CORSFilter());

    }
}
