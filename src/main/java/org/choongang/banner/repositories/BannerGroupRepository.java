package org.choongang.banner.repositories;

import org.choongang.banner.entities.BannerGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface BannerGroupRepository extends JpaRepository<BannerGroup, String>, QuerydslPredicateExecutor<BannerGroup> {

}
