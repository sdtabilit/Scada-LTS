package utils;

import com.serotonin.db.IntValuePair;

public class IntValuePairPrinted extends IntValuePair {

    public IntValuePairPrinted(IntValuePair intValuePair) {
        super(intValuePair.getKey(), intValuePair.getValue());
    }

    public IntValuePair getIntValuePair() {
        return this;
    }

    @Override
    public String toString() {
        return "{" +
                "key=" + super.getKey() +
                ", value=" + super.getValue() +
                '}';
    }
}
