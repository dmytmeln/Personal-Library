package org.example.library.recommendations.repository;

import org.example.library.recommendations.domain.UserProfileVector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileVectorRepository extends JpaRepository<UserProfileVector, Integer> {
}
