package org.choongang.upbit.repositories;

import org.choongang.upbit.entities.UpBitTicker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface UpBitTickerRepository extends JpaRepository<UpBitTicker, String>, QuerydslPredicateExecutor<UpBitTicker> {
}
