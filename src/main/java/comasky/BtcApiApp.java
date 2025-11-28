package comasky;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class BtcApiApp {
    public static void main(String[] args) {
        Quarkus.run(args);
    }
}