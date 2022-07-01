package at.emielregis.backend.data.dtos;

import lombok.AllArgsConstructor;

import java.util.Objects;

@AllArgsConstructor
public class TransientClassId {
    private String id;

    public static TransientClassId of(String itemName) {
        return new TransientClassId(itemName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransientClassId transientClassId = (TransientClassId) o;
        return Objects.equals(id, transientClassId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
