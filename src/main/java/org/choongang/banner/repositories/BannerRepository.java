package org.choongang.banner.repositories;

import org.choongang.banner.entities.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface BannerRepository extends JpaRepository<Banner, Long>, QuerydslPredicateExecutor<Banner> {

}
