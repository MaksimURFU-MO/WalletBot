package client;

import java.io.IOException;

@FunctionalInterface
public interface RateSource {
    String fetch() throws IOException;
}