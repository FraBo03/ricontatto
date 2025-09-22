package com.ricontatto.repository;

import com.ricontatto.model.FasciaOraria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FasciaOrariaRepository extends JpaRepository<FasciaOraria, Long> {
    // eventualmente puoi aggiungere metodi custom qui
}
