package benchmark;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BenchmarkService {
    protected final static Logger logger = LogManager.getLogger(BenchmarkService.class);

    public static void printBenchmarks(List<Benchmark> benchmarks) {
        for(Benchmark b : benchmarks) {
            logger.info(b.toString());
        }
    }

    

    
}
