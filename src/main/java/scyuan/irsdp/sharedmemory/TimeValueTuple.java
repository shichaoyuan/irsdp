package scyuan.irsdp.sharedmemory;

import java.util.Objects;

public class TimeValueTuple {
    public final long timestamp;
    public final Object value;

    public TimeValueTuple(long timestamp, Object value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeValueTuple that = (TimeValueTuple) o;
        return timestamp == that.timestamp &&
                value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, value);
    }
}
