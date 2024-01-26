package org.choongang.order.repositories;

import org.choongang.order.entities.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long>, QuerydslPredicateExecutor<OrderStatusHistory> {
}
