package at.emielregis.backend.repository;

import at.emielregis.backend.data.entities.ClassID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassIdRepository extends JpaRepository<ClassID, Long> {
    boolean existsByClassId(String classId);

    ClassID getByClassId(String classId);
}
