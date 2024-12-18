package ru.muhametshin.practice5.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.muhametshin.practice5.entity.File;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
}
