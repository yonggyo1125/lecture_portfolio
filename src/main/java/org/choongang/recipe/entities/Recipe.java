package org.choongang.recipe.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.choongang.commons.entities.Base;
import org.choongang.member.entities.Member;

import java.util.UUID;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Recipe extends Base {

    @Id
    @GeneratedValue
    private Long seq;

    @Column(length=65, nullable = false)
    private String gid = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="memberSeq")
    private Member member;

    @Column(length=100, nullable = false)
    private String rcpName;

    @Lob
    private String rcpInfo;

    private int estimatedT;

    @Column(length=60)
    private String category;

    @Column(length=60)
    private String subCategory;

    @Lob
    private String requiredIng;

    @Lob
    private String subIng;

    @Lob
    private String condiments;
}
