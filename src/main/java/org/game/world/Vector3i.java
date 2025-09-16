package org.game.world;

public record Vector3i(int x, int y, int z) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vector3i that)) return false;
        return x == that.x && y == that.y && z == that.z;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}

