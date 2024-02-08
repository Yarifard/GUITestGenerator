package ir.ac.um.guitestgenerating.TestUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public  class BuildPhaseStreamGobbler implements Runnable {
    private InputStream inputStream;
    private Consumer<String> consumer;
    private BufferedReader buffer;
    AtomicBoolean falg;


    public BuildPhaseStreamGobbler(InputStream inputStream, Consumer<String> consumer) {
        this.inputStream = inputStream;
        this.consumer = consumer;
        this.falg = new AtomicBoolean(false);
    }

    @Override
    public void run() {
         new BufferedReader(new InputStreamReader(inputStream)).lines()
                .forEach(consumer);
        buffer = new BufferedReader(new InputStreamReader(inputStream));
        buffer.lines().forEach(line->{
            consumer.accept(line);
            if(line.contains("BUILD SUCCESSFUL in"))
                falg.set(true);
        });
    }

    public boolean getStatus(){
        return falg.get();
    }


}