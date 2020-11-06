package benchmarks;

import com.serotonin.mango.rt.dataImage.types.*;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomValue {

    public static MangoValue generate() {
        List<MangoValue> values = new ArrayList<>();
        Random random = new Random();
        values.add(new BinaryValue(random.nextBoolean()));
        values.add(new NumericValue(random.nextDouble()));
        values.add(new MultistateValue(random.nextInt(99999)));
        values.add(new AlphanumericValue(RandomStringUtils.randomAlphanumeric(12)));
        return values.get(random.nextInt(4));
    }

    public static MangoValue generateBoolean() {
        Random random = new Random();
        return new BinaryValue(random.nextBoolean());
    }

    public static MangoValue generateNumeric() {
        Random random = new Random();
        return new NumericValue(random.nextDouble());
    }
}
