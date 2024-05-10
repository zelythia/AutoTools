package net.zelythia;

import java.util.Objects;

public class ItemMiningSpeed {
    public Float miningSpeed;
    public int priority;

    ItemMiningSpeed(Float miningSpeed, int priority) {
        this.miningSpeed = miningSpeed;
        this.priority = priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemMiningSpeed that = (ItemMiningSpeed) o;
        return priority == that.priority && Objects.equals(miningSpeed, that.miningSpeed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(miningSpeed, priority);
    }

    @Override
    public String toString() {
        return "ItemMiningSpeed("+miningSpeed+","+priority+")";
    }
}
