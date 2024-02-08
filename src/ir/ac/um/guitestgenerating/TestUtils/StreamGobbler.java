package ir.ac.um.guitestgenerating.TestUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class StreamGobbler implements Runnable{

        private InputStream inputStream;
        private Consumer<String> consumer;
        private BufferedReader buffer;
        AtomicBoolean flag;


        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
            this.flag = new AtomicBoolean(false);
        }

        @Override
        public void run() {
//            new BufferedReader(new InputStreamReader(inputStream)).lines()
//                    .forEach(consumer);
            buffer = new BufferedReader(new InputStreamReader(inputStream));
            buffer.lines().forEach(line->{
                if(line.contains("OK (1 test)"))
                    flag.set(true);
                consumer.accept(line);

            });
      //      buffer.lines().forEach(consumer);

        }
        public boolean getStatus(){
            return flag.get();
        }


}
