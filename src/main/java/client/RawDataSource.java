package client;

import java.io.IOException;

public interface RawDataSource {
    String fetch() throws IOException;
}