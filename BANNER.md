# 배너 관리 

## 관리자 메뉴 추가 

> admin/menus/Menu.java

```java
...

public class Menu {
    ...
    static {
        
        ...

        menus.put("banner", Arrays.asList(
                new MenuDetail("group", "배너관리", "/admin/banner"),
                new MenuDetail("add", "배너등록", "/admin/banner/group")
        ));
        
        ...
    }
    
    ...
    
}
```

> resources/templates/admin/outlines/_side.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
    <aside th:fragment="menus">
        ...
        
        <a th:href="@{/admin/banner}" th:classappend="${menuCode} == 'banner' ? 'on'">
            <i class="xi-image"></i> 배너관리
        </a>
        
        ...
        
    </aside>
</html>
```

## 엔티티 및 레포지토리 구성

> banner/entities/BannerGroup.java

```java
package org.choongang.banner.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.choongang.commons.entities.BaseMember;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class BannerGroup extends BaseMember {
    @Id
    private String groupCode;

    @Column(length=80, nullable = false)
    private String groupName; // 배너 그룹명

    private boolean active; // 사용 여부 
}
```

> banner/entities/Banner.java

```java
package org.choongang.banner.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.choongang.commons.entities.BaseMember;
import org.choongang.file.entities.FileInfo;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = @Index(name="idx_banner_basic", columnList = "listOrder DESC, createdAt ASC"))
public class Banner extends BaseMember {
    @Id
    @GeneratedValue
    private Long seq;

    @Column(length=60, nullable = false)
    private String bName; // 배너명

    private String bLink; // 배너 링크
    private String target = "_self"; // 배너 타켓 

    private long listOrder; // 진열 순서

    private boolean active; // 노출 여부

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="groupCode")
    private BannerGroup bannerGroup;

    @Transient
    private FileInfo bannerImage;
}
```

> banner/repositories/BannerGroupRepository.java

```java
package org.choongang.banner.repositories;

import org.choongang.banner.entities.BannerGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface BannerGroupRepository extends JpaRepository<BannerGroup, String>, QuerydslPredicateExecutor<BannerGroup> {

}
```

> banner/repositories/BannerRepository.java

```java
package org.choongang.banner.repositories;

import org.choongang.banner.entities.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface BannerRepository extends JpaRepository<Banner, Long>, QuerydslPredicateExecutor<Banner> {

}
```

## 서비스 구성

> resources/messages/validations.properties

```properties
...

# 관리자 - 배너관리
NotBlank.requestBanner.groupCode=배너 그룹을 선택하세요.
NotBlank.requestBanner.bName=배너명을 입력하세요.
NotBlank.requestBannerGroup.groupCode=배너 그룹을 입력하세요.
NotBlank.requestBannerGroup.groupName=배너 그룹명을 입력하세요.
Duplicated.requestBannerGroup.groupCode=이미 등록된 그룹코드 입니다.
NotFound.requestBanner.files=배너 이미지를 업로드하세요.
NotNull.requestBanner.files=배너 이미지를 업로드하세요.
```

> resources/messages/errors.properties

```properties
...


NotFound.bannerGroup=등록되지 않은 배너그룹입니다.
NotFound.banner=등록되지 않은 배너입니다.
```

> admin/banner/controllers/RequestBannerGroup.java

```java
package org.choongang.admin.banner.controllers;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestBannerGroup {
    @NotBlank
    private String groupCode;

    @NotBlank
    private String groupName;

    private boolean active;
}
```

> admin/banner/controllers/RequestBanner.java

```java
package org.choongang.admin.banner.controllers;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.choongang.file.entities.FileInfo;
import org.springframework.web.multipart.MultipartFile;

@Data
public class RequestBanner {

    private String mode = "add";

    private Long seq;

    @NotBlank
    private String groupCode; // 배너 그룹 코드

    @NotBlank
    private String bName;

    private String bLink; // 배너 링크
    private String target = "_self";

    private long listOrder;

    private boolean active;

    private MultipartFile[] files;

    private FileInfo bannerImage;
}
```

> banner/service/BannerGroupNotFoundException.java

```java
package org.choongang.banner.service;

import org.choongang.commons.Utils;
import org.choongang.commons.exceptions.AlertBackException;
import org.springframework.http.HttpStatus;

public class BannerGroupNotFoundException extends AlertBackException {
    public BannerGroupNotFoundException() {
        super(Utils.getMessage("NotFound.bannerGroup", "errors"), HttpStatus.NOT_FOUND);
    }
}
```

> banner/service/BannerNotFoundException.java

```java
package org.choongang.banner.service;

import org.choongang.commons.Utils;
import org.choongang.commons.exceptions.AlertBackException;
import org.springframework.http.HttpStatus;

public class BannerNotFoundException extends AlertBackException {
    public BannerNotFoundException() {
        super(Utils.getMessage("NotFound.banner", "errors"), HttpStatus.NOT_FOUND);
    }
}
```

> banner/service/BannerGroupSaveService.java

```java
package org.choongang.banner.service;

import lombok.RequiredArgsConstructor;
import org.choongang.admin.banner.controllers.RequestBannerGroup;
import org.choongang.banner.entities.BannerGroup;
import org.choongang.banner.repositories.BannerGroupRepository;
import org.choongang.commons.Utils;
import org.choongang.commons.exceptions.AlertException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerGroupSaveService {
    private final BannerGroupRepository groupRepository;
    private final Utils utils;

    /**
     * 배너 그룹 추가 & 수정
     *
     * @param form
     */
    public void save(RequestBannerGroup form) {
        String groupCode = form.getGroupCode();

        BannerGroup data = groupRepository.findById(groupCode).orElseGet(() -> {
            BannerGroup bannerGroup = new BannerGroup();
            bannerGroup.setGroupCode(form.getGroupCode());
            return bannerGroup;
        });

        data.setGroupName(form.getGroupName());
        data.setActive(form.isActive());

        groupRepository.saveAndFlush(data);
    }

    /**
     * 배너 그룹 목록 수정
     *
     * @param chks
     */
    public void saveList(List<Integer> chks) {
        if (chks == null || chks.isEmpty()) {
            throw new AlertException("수정할 배너 그룹을 선택하세요.", HttpStatus.BAD_REQUEST);
        }

        for (int chk : chks) {
            String groupCode = utils.getParam("groupCode_" + chk);
            BannerGroup bannerGroup = groupRepository.findById(groupCode).orElse(null);
            if (bannerGroup == null) continue;

            bannerGroup.setGroupName(utils.getParam("groupName_" + chk));
            bannerGroup.setActive(Boolean.parseBoolean(utils.getParam("active_" + chk)));
        }

        groupRepository.flush();
    }
}
```

> banner/service/BannerGroupDeleteService.java

```java
package org.choongang.banner.service;

import lombok.RequiredArgsConstructor;
import org.choongang.banner.entities.Banner;
import org.choongang.banner.entities.BannerGroup;
import org.choongang.banner.repositories.BannerGroupRepository;
import org.choongang.commons.Utils;
import org.choongang.commons.exceptions.AlertException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerGroupDeleteService {
    private final BannerGroupRepository groupRepository;
    private final BannerInfoService bannerInfoService;
    private final BannerDeleteService bannerDeleteService;
    private final Utils utils;

    public void delete(String groupCode) {
        BannerGroup data = bannerInfoService.getGroup(groupCode, true);
        List<Banner> banners = data.getBanners();
        if (banners != null && !banners.isEmpty()) {
            banners.forEach(b -> bannerDeleteService.delete(b.getSeq()));
        }

        groupRepository.delete(data);
    }

    
    public void deleteList(List<Integer> chks) {
        if (chks == null || chks.isEmpty()) {
            throw new AlertException("삭제할 배너 그룹을 선택하세요.", HttpStatus.BAD_REQUEST);
        }

        for (int chk : chks) {
            String groupCode = utils.getParam("groupCode_" + chk);
            BannerGroup data = groupRepository.findById(groupCode).orElse(null);
            if (data == null) continue;

            groupRepository.delete(data);
        }

        groupRepository.flush();
    }
}
```

> banner/service/BannerSaveService.java

```java
package org.choongang.banner.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.choongang.admin.banner.controllers.RequestBanner;
import org.choongang.banner.entities.Banner;
import org.choongang.banner.entities.BannerGroup;
import org.choongang.banner.repositories.BannerGroupRepository;
import org.choongang.banner.repositories.BannerRepository;
import org.choongang.commons.Utils;
import org.choongang.commons.exceptions.AlertException;
import org.choongang.file.service.FileUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BannerSaveService {
    private final BannerGroupRepository groupRepository;
    private final BannerRepository repository;
    private final FileUploadService uploadService;
    private final Utils utils;

    public void save(RequestBanner form) {
        String mode = form.getMode();
        Long seq = form.getSeq();

        mode = StringUtils.hasText(mode) ? mode : "add";

        Banner banner = null;
        if (mode.equals("edit") && seq != null) {
            banner = repository.findById(seq).orElseThrow(BannerNotFoundException::new);
        } else {
            banner = new Banner();
            BannerGroup bannerGroup = groupRepository.findById(form.getGroupCode()).orElseThrow(BannerGroupNotFoundException::new);
            banner.setBannerGroup(bannerGroup);
        }

        banner.setBName(form.getBName());
        banner.setListOrder(form.getListOrder());
        banner.setActive(form.isActive());
        banner.setBLink(form.getBLink());
        banner.setTarget(form.getTarget());

        repository.save(banner);

        String groupCode = banner.getBannerGroup().getGroupCode();
        if (form.getFiles() != null && !form.getFiles()[0].isEmpty()) {
            try {
                uploadService.upload(form.getFiles(), groupCode, "banner_" + banner.getSeq(), true, true);
                uploadService.processDone(groupCode);
            } catch (Exception e) { }
        }
    }

    public void saveList(List<Integer> chks) {
        if (chks == null || chks.isEmpty()) {
            throw new AlertException("수정할 배너를 선택하세요.", HttpStatus.BAD_REQUEST);
        }

        for (int chk : chks) {
            Long seq = Long.valueOf(utils.getParam("seq_" + chk));
            boolean active = Boolean.parseBoolean(utils.getParam("active_" + chk));

            Banner banner = repository.findById(seq).orElse(null);
            if (banner == null) continue;

            banner.setActive(active);
        }

        repository.flush();
    }
}
```

> banner/service/BannerInfoService.java

```java
package org.choongang.banner.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.choongang.admin.banner.controllers.BannerGroupSearch;
import org.choongang.admin.banner.controllers.RequestBanner;
import org.choongang.banner.entities.Banner;
import org.choongang.banner.entities.BannerGroup;
import org.choongang.banner.entities.QBanner;
import org.choongang.banner.entities.QBannerGroup;
import org.choongang.banner.repositories.BannerGroupRepository;
import org.choongang.banner.repositories.BannerRepository;
import org.choongang.commons.ListData;
import org.choongang.commons.Pagination;
import org.choongang.commons.Utils;
import org.choongang.file.entities.FileInfo;
import org.choongang.file.service.FileInfoService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

import static org.springframework.data.domain.Sort.Order.asc;
import static org.springframework.data.domain.Sort.Order.desc;

@Service
@RequiredArgsConstructor
public class BannerInfoService {
    private final BannerRepository bannerRepository;
    private final BannerGroupRepository bannerGroupRepository;
    private final FileInfoService fileInfoService;
    private final HttpServletRequest request;

    public Banner get(Long seq) {
        Banner banner = bannerRepository.findById(seq).orElseThrow(BannerNotFoundException::new);

        addBannerInfo(banner);

        return banner;
    }

    public RequestBanner getForm(Long seq) {
        Banner data = get(seq);
        RequestBanner form = new ModelMapper().map(data, RequestBanner.class);
        form.setBannerImage(data.getBannerImage());
        form.setGroupCode(data.getBannerGroup().getGroupCode());
        form.setMode("edit");

        return form;
    }

    /**
     * 그룹 코드 별 배너 목록
     *
     * @param groupCode
     * @param isAll
     * @return
     */
    public List<Banner> getList(String groupCode, boolean isAll) {
        QBanner banner = QBanner.banner;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(banner.bannerGroup.groupCode.eq(groupCode));

        if (!isAll) {
            builder.and(banner.active.eq(true));
        }

        List<Banner> items = (List<Banner>)bannerRepository.findAll(builder, Sort.by(desc("listOrder"), asc("createdAt")));

        items.forEach(this::addBannerInfo);

        return items;
    }

    public List<Banner> getList(String groupCode) {
        return getList(groupCode, false);
    }

    /**
     * 배너 그룹
     *
     * @param groupCode
     * @param isAll
     * @return
     */
    public BannerGroup getGroup(String groupCode, boolean isAll) {
        BannerGroup group = bannerGroupRepository.findById(groupCode).orElseThrow(BannerGroupNotFoundException::new);

        List<Banner> banners = getList(groupCode, isAll);
        group.setBanners(banners);

        return group;
    }

    public BannerGroup getGroup(String groupCode) {
        return getGroup(groupCode, false);
    }

    /**
     * 배너 그룹 목록
     *
     * @return
     */
    public ListData<BannerGroup> getGroupList(BannerGroupSearch search) {
        int page = Utils.onlyPositiveNumber(search.getPage(), 1);
        int limit = Utils.onlyPositiveNumber(search.getLimit(), 20);

        BooleanBuilder builder = new BooleanBuilder();
        QBannerGroup bannerGroup = QBannerGroup.bannerGroup;

        /* 검색 조건 처리 S */
        String sopt = search.getSopt();
        String skey = search.getSkey();

        sopt = StringUtils.hasText(sopt) ? sopt : "all";
        if (StringUtils.hasText(skey)) {
            skey = skey.trim();
            BooleanExpression groupCodeCond = bannerGroup.groupCode.contains(skey);
            BooleanExpression groupNameCond = bannerGroup.groupName.contains(skey);

            if (sopt.equals("groupCode")) { // 그룹 코드
                builder.and(groupCodeCond);
            } else if (sopt.equals("groupName")) { // 그룹명
                builder.and(groupNameCond);

            } else { // 통합 검색
                BooleanBuilder orBuilder = new BooleanBuilder();
                builder.and(orBuilder.or(groupCodeCond).or(groupNameCond));
            }
        }
        /* 검색 조건 처리 E */

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(desc("createdAt")));

        Page<BannerGroup> data = bannerGroupRepository.findAll(builder, pageable);
        int total = (int)bannerGroupRepository.count(builder);

        Pagination pagination = new Pagination(page, total, 10, limit, request);

        return new ListData<>(data.getContent(), pagination);
    }


    private void addBannerInfo(Banner banner) {
        String groupCode = banner.getBannerGroup().getGroupCode();
        List<FileInfo> banners = fileInfoService.getListDone(groupCode, "banner_" + banner.getSeq());
        if (banners != null && !banners.isEmpty()) {
            banner.setBannerImage(banners.get(0));
        }
    }
}
```

> banner/service/BannerDeleteService.java

```java
package org.choongang.banner.service;

import lombok.RequiredArgsConstructor;
import org.choongang.banner.entities.Banner;
import org.choongang.banner.repositories.BannerRepository;
import org.choongang.commons.Utils;
import org.choongang.commons.exceptions.AlertException;
import org.choongang.file.entities.FileInfo;
import org.choongang.file.service.FileDeleteService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BannerDeleteService {
    private final BannerInfoService bannerInfoService;
    private final BannerRepository bannerRepository;
    private final FileDeleteService fileDeleteService;
    private final Utils utils;

    public void delete(Long seq) {
        Banner banner = bannerInfoService.get(seq);

        FileInfo fileInfo = banner.getBannerImage();
        if (fileInfo != null) {
            fileDeleteService.delete(fileInfo.getSeq());
        }

        bannerRepository.delete(banner);
        bannerRepository.flush();
    }

    public void deleteList(List<Integer> chks) {
        if (chks == null || chks.isEmpty()) {
            throw new AlertException("삭제할 배너를 선택하세요.", HttpStatus.BAD_REQUEST);
        }

        for (int chk : chks) {
            Long seq = Long.valueOf(utils.getParam("seq_" + chk));
            delete(seq);
        }
    }
}
```

## 컨트롤러 구성

> admin/banner/controllers/BannerGroupValidator.java 

```java 
package org.choongang.admin.banner.controllers;

import lombok.RequiredArgsConstructor;
import org.choongang.banner.repositories.BannerGroupRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class BannerGroupValidator implements Validator {

    private final BannerGroupRepository bannerGroupRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(RequestBannerGroup.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        RequestBannerGroup form = (RequestBannerGroup) target;

        /**
         * 배너 코드 중복 등록 여부 체크
         */
        String groupCode = form.getGroupCode();
        if (StringUtils.hasText(groupCode) && bannerGroupRepository.existsById(groupCode)) {
            errors.rejectValue("groupCode", "Duplicated");
        }
    }
}
```

> admin/banner/controllers/BannerValidator.java

```java 
package org.choongang.admin.banner.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class BannerValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(RequestBanner.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        RequestBanner form = (RequestBanner)target;

        String mode = form.getMode();
        MultipartFile[] files = form.getFiles();
        if ((mode == null || mode.equals("add")) && files == null || files.length == 0) {
            errors.rejectValue("files", "NotFound");
        }
    }
}
```

> admin/banner/controllers/BannerController.java

```java
package org.choongang.admin.banner.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.choongang.admin.menus.Menu;
import org.choongang.admin.menus.MenuDetail;
import org.choongang.banner.entities.Banner;
import org.choongang.banner.entities.BannerGroup;
import org.choongang.banner.service.*;
import org.choongang.commons.ExceptionProcessor;
import org.choongang.commons.ListData;
import org.choongang.commons.Utils;
import org.choongang.commons.exceptions.AlertException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller("adminBannerController")
@RequestMapping("/admin/banner")
@RequiredArgsConstructor
public class BannerController implements ExceptionProcessor {

    private final BannerGroupSaveService bannerGroupSaveService;
    private final BannerGroupDeleteService bannerGroupDeleteService;
    private final BannerInfoService bannerInfoService;
    private final BannerSaveService bannerSaveService;
    private final BannerDeleteService bannerDeleteService;

    private final BannerValidator bannerValidator;
    private final BannerGroupValidator bannerGroupValidator;

    @ModelAttribute("menuCode")
    public String menuCode() {
        return "banner";
    }

    @ModelAttribute("subMenus")
    public List<MenuDetail> subMenus() {
        return Menu.getMenus("banner");
    }


    @ModelAttribute("bannerGroups")
    public List<BannerGroup> bannerGroups() {
        BannerGroupSearch search = new BannerGroupSearch();
        search.setLimit(10000);
        ListData<BannerGroup> data = bannerInfoService.getGroupList(search);

        return data.getItems();
    }

    /**
     * 배너 그룹 관리
     *
     * @param model
     * @return
     */
    @GetMapping
    public String group(@ModelAttribute  BannerGroupSearch search,  Model model) {
        commonProcess("group", model);

        ListData<BannerGroup> data = bannerInfoService.getGroupList(search);

        model.addAttribute("items", data.getItems());
        model.addAttribute("pagination", data.getPagination());

        return "admin/banner/group";
    }

    /**
     * 그룹 등록
     *
     * @param form
     * @param errors
     * @param model
     * @return
     */
    @PostMapping
    public String addGroup(@Valid RequestBannerGroup form, Errors errors,  Model model) {
        commonProcess("group", model);

        bannerGroupValidator.validate(form, errors);

        if (errors.hasErrors()) {
            String code = errors.getFieldErrors().stream().map(f -> f.getCodes()[0]).findFirst().orElse(null);
            if (code != null) throw new AlertException(Utils.getMessage(code), HttpStatus.BAD_REQUEST);
        }

        bannerGroupSaveService.save(form);

        model.addAttribute("script", "parent.location.reload();");
        return "common/_execute_script";
    }

    /**
     * 그룹 목록 수정
     *
     * @param chks
     * @param model
     * @return
     */
    @PatchMapping
    public String editGroup(@RequestParam(name="chk", required = false) List<Integer> chks, Model model) {
        commonProcess("group", model);


        bannerGroupSaveService.saveList(chks);

        model.addAttribute("script", "parent.location.reload();");
        return "common/_execute_script";
    }

    /**
     * 그룹 목록 삭제
     *
     * @param chks
     * @param model
     * @return
     */
    @DeleteMapping
    public String deleteGroup(@RequestParam(name="chk", required = false) List<Integer> chks, Model model) {
        commonProcess("group", model);

        bannerGroupDeleteService.deleteList(chks);

        model.addAttribute("script", "parent.location.reload();");
        return "common/_execute_script";
    }

    /**
     * 배너 등록
     *
     * @param model
     * @return
     */
    @GetMapping("/add")
    public String add(@ModelAttribute RequestBanner form,  Model model) {
        commonProcess("add", model);

        return "admin/banner/add";
    }

    /**
     * 배너 수정
     * @param seq
     * @param model
     * @return
     */
    @GetMapping("/edit/{seq}")
    public String edit(@PathVariable("seq") Long seq, Model model) {
        commonProcess("edit", model);

        RequestBanner form = bannerInfoService.getForm(seq);
        model.addAttribute("requestBanner", form);

        return "admin/banner/edit";
    }

    /**
     * 배너 추가, 수정
     *
     * @param form
     * @param errors
     * @param model
     * @return
     */
    @PostMapping("/save")
    public String save(@Valid RequestBanner form, Errors errors, Model model) {
        commonProcess(form.getMode(), model);

        bannerValidator.validate(form, errors);

        if (errors.hasErrors()) {
            return "admin/banner/" + form.getMode();
        }

        bannerSaveService.save(form);

        return "redirect:/admin/banner/list/" + form.getGroupCode();
    }

    /**
     * 배너 목록
     *
     * @param groupCode
     * @param model
     * @return
     */
    @GetMapping("/list/{groupCode}")
    public String bannerList(@PathVariable("groupCode") String groupCode, Model model) {
        commonProcess("list", model);

        List<Banner> items = bannerInfoService.getList(groupCode, true);

        model.addAttribute("items", items);

        return "admin/banner/list";
    }

    @PatchMapping("/list")
    public String editBannerList(@RequestParam("chk") List<Integer> chks, Model model) {
        commonProcess("list", model);

        bannerSaveService.saveList(chks);

        model.addAttribute("script", "parent.location.reload();");
        return "common/_execute_script";
    }

    @DeleteMapping("/list")
    public String deleteBannerList(@RequestParam("chk") List<Integer> chks, Model model) {
        commonProcess("list", model);

        bannerDeleteService.deleteList(chks);

        model.addAttribute("script", "parent.location.reload();");
        return "common/_execute_script";
    }

    /**
     * 배너 공통 처리
     *
     * @param mode
     * @param model
     */
    private void commonProcess(String mode, Model model) {
        String pageTitle = "배너목록";
        mode = StringUtils.hasText(mode) ? mode : "list";

        List<String> addCommonScript = new ArrayList<>();
        List<String> addScript = new ArrayList<>();

        if (mode.equals("group")) {
            pageTitle = "배너그룹";
        } else if (mode.equals("add") || mode.equals("edit")) {
            pageTitle = "배너" + ((mode.equals("edit")) ? "수정" : "등록");
            addCommonScript.add("fileManager");
            addScript.add("banner/form");
        }

        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("subMenuCode", mode);
    }
}

```

> resources/templates/admin/banner/group.html
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{admin/layouts/main}">
<section layout:fragment="content">
    <h1>배너 관리</h1>
    <h2>그룹 등록</h2>
    <form name="frmSave" method="post" th:action="@{/admin/banner}" autocomplete="off" target="ifrmProcess">
        <table class="table_cols">
            <tr>
                <th width="100">그룹코드</th>
                <td width="300">
                    <input type="text" name="groupCode">
                </td>
                <th width="100">그룹명</th>
                <td>
                    <input type="text" name="groupName">
                </td>
            </tr>
        </table>
        <div class="search_btn">
            <button type="submit" class="btn">등록하기</button>
        </div>
    </form>
    <h2>그룹 목록</h2>
    <form name="frmSearch" method="get" th:action="@{/admin/banner}" autocomplete="off" th:object="${bannerGroupSearch}">
        <div class="mb10 input_grp">
            <select name="sopt" th:field="*{sopt}">
                <option value="all">통합검색</option>
                <option value="groupCode">그룹코드</option>
                <option value="groupName">그룹명</option>
            </select>
            <input type="text" name="skey" placeholder="키워드 입력" th:field="*{skey}" class="w200">
            <button type="submit" class="sbtn on">
                <i class="xi-search"></i> 검색하기
            </button>
        </div>
    </form>
    <form name="frmList" method="post" th:action="@{/admin/banner}" target="ifrmProcess" autocomplete="off">
        <input type="hidden" name="_method" value="PATCH">
        <table class="table_rows">
            <thead>
            <tr>
                <th width="40" align="center">
                    <input type="checkbox" class="checkall" data-target-name="chk" id="checkall">
                    <label for="checkall"></label>
                </th>
                <th width="150">그룹코드</th>
                <th width="250">그룹명</th>
                <th width="250">사용여부</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <tr th:if="${items == null || items.isEmpty()}">
                <td colspan="5" class="no_data">조회된 배너그룹이 없습니다.</td>
            </tr>
            <tr th:unless="${items == null || items.isEmpty()}" th:each="item, status : ${items}" th:object="${item}">
                <td>
                    <input type="hidden" th:name="${'groupCode_' + status.index}" th:value="*{groupCode}">
                    <input type="checkbox" name="chk" th:value="${status.index}" th:id="${'chk_' + status.index}">
                    <label th:for="${'chk_' + status.index}"></label>
                </td>
                <td th:text="*{groupCode}"></td>
                <td>
                    <input type="text" th:name="${'groupName_' + status.index}" th:value="*{groupName}">
                </td>
                <td>
                    <input type="radio" th:name="${'active_' + status.index}" value="true" th:checked="*{active}" th:id="${'active_' + status.index + '_true'}">
                    <label th:for="${'active_' + status.index + '_true'}">사용</label>
                    <input type="radio" th:name="${'active_' + status.index}" value="false" th:checked="*{!active}" th:id="${'active_' + status.index + '_false'}">
                    <label th:for="${'active_' + status.index + '_false'}">미사용</label>
                </td>
                <td>
                    <a th:href="@{/admin/banner/add(groupCode=*{groupCode})}" class="sbtn"><i class="xi-plus"></i> 배너등록</a>
                    <a th:href="@{/admin/banner/list/{groupCode}(groupCode=*{groupCode})}" class="sbtn on"><i class="xi-list"></i> 배너목록</a>
                </td>
            </tr>
            </tbody>
        </table>
        <div class="table_actions" th:unless="${items == null || items.isEmpty()}">
            <button type="button" data-mode="delete" data-form-name="frmList" class="form_action sbtn">선택 배너그룹 삭제</button>
            <button type="button" data-mode="edit" data-form-name="frmList" class="form_action sbtn on">선택 배너그룹 수정</button>
        </div>
    </form>
    <th:block th:replace="~{common/_pagination::pagination}"></th:block>
</section>
</html>
```

> resources/templates/admin/banner/add.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{admin/layouts/main}">
<section layout:fragment="content">
    <h1>배너 등록</h1>
    <form name="frmSave" method="post" th:action="@{/admin/banner/save}" autocomplete="off" enctype="multipart/form-data">
        <input type="hidden" name="mode" value="add">

        <th:block th:replace="~{admin/banner/_form::form}"></th:block>

        <div class="submit_btns">
            <button type="reset" class="btn">다시입력</button>
            <button type="submit" class="btn">등록하기</button>
        </div>
    </form>
</section>
</html>
```

> resources/templates/admin/banner/edit.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{admin/layouts/main}">
<section layout:fragment="content">
    <h1>배너 등록</h1>
    <form name="frmSave" method="post" th:action="@{/admin/banner/save}" autocomplete="off" enctype="multipart/form-data">
        <input type="hidden" name="mode" value="edit">
        <th:block th:replace="~{admin/banner/_form::form}"></th:block>

        <div class="submit_btns">
            <button type="reset" class="btn">다시입력</button>
            <button type="submit" class="btn">수정하기</button>
        </div>
    </form>
</section>
</html>
```

> resources/templates/admin/banner/_form.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<th:block th:fragment="form" th:object="${requestBanner}">
    <input th:if="*{mode == 'edit' && seq != null}" type="hidden" name="seq" th:field="*{seq}">
    <div class="error global" th:each="err : ${#fields.globalErrors()}" th:text="${err}"></div>
    <table class="table_cols">
        <tr>
            <th width="180">배너그룹</th>
            <td>
                <select name="groupCode" th:field="*{groupCode}">
                    <option th:each="item : ${bannerGroups}" th:value="${item.groupCode}" th:text="${item.groupName}"></option>
                </select>
                <div class="error" th:each="err : ${#fields.errors('groupCode')}" th:text="${err}"></div>
            </td>
        </tr>
        <tr>
            <th>배너명</th>
            <td>
                <input type="text" name="bName" th:field="*{bName}">
                <div class="error" th:each="err : ${#fields.errors('bName')}" th:text="${err}"></div>
            </td>
        </tr>
        <tr>
            <th>배너링크</th>
            <td>
                <div class="input_grp">
                    <input type="text" name="bLink" th:field="*{bLink}">
                    <select name="target" th:field="*{target}">
                        <option value="_self">_self</option>
                        <option value="_parent">_parent</option>
                        <option value="_top">_top</option>
                        <option value="_blank">_blank</option>
                    </select>
                </div>
                <div class="error" th:each="err : ${#fields.errors('bLink')}" th:text="${err}"></div>
            </td>
        </tr>
        <tr>
            <th>사용여부</th>
            <td>
                <input type="radio" name="active" value="true" id="active_true" th:field="*{active}">
                <label for="active_true">사용</label>

                <input type="radio" name="active" value="false" id="active_false" th:field="*{active}">
                <label for="active_false">미사용</label>
                <div class="error" th:each="err : ${#fields.errors('active')}" th:text="${err}"></div>
            </td>
        </tr>
        <tr>
            <th>진열가중치</th>
            <td>
                <input type="number" name="listOrder" th:field="*{listOrder}">
                <div class="error" th:each="err : ${#fields.errors('listOrder')}" th:text="${err}"></div>
            </td>
        </tr>
        <tr>
            <th>배너이미지</th>
            <td>
                <a th:if="*{bannerImage != null}" th:href="*{bannerImage.fileUrl}" target="_blank">
                    <img th:src="*{bannerImage.fileUrl}" style="max-width: 600px; display: block;">
                </a>
                <input type="file" name="files" th:field="*{files}">
                <div class="error" th:each="err : ${#fields.errors('files')}" th:text="${err}"></div>
            </td>
        </tr>
    </table>
</th:block>
</html>
```

> resources/templates/admin/banner/list.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{admin/layouts/main}">
<section layout:fragment="content">
    <h1>배너 목록</h1>
    <form name="frmList" method="post" th:action="@{/admin/banner/list}" target="ifrmProcess" autocomplete="off">
        <input type="hidden" name="_method" value="PATCH">
        <table class="table_rows">
            <thead>
                <tr>
                    <th width="40">
                        <input type="checkbox" class="checkall" id="checkall" data-target-name="chk">
                        <label for="checkall"></label>
                    </th>
                    <th width="400" colspan="2">배너</th>
                    <th width="200">사용여부</th>
                    <th></th>
                </tr>
            </thead>
            <tbody>
                <tr th:if="${items == null || items.isEmpty()}">
                    <td colspan="4" class="no_data">등록된 배너가 없습니다.</td>
                </tr>
                <tr th:unless="${items == null || items.isEmpty()}" th:each="item, status : ${items}" th:object="${item}">
                    <td>
                        <input type="hidden" th:name="${'seq_' + status.index}" th:value="*{seq}">
                        <input type="checkbox" name="chk" th:value="${status.index}" th:id="${'chk_' + status.index}">
                        <label th:for="${'chk_' + status.index}"></label>
                    </td>
                    <td width="100">
                        <a th:if="*{bannerImage != null}" th:utext="*{@utils.printThumb(bannerImage.seq, 100, 100, 'image')}" th:href="*{bannerImage.fileUrl}" target="_blank"></a>
                    </td>
                    <td th:text="*{bName}" width="300"></td>
                    <td align="center">
                        <input type="radio" th:name="${'active_' + status.index}" value="true" th:checked="*{active}" th:id="${'active_' + status.index + '_true'}">
                        <label th:for="${'active_' + status.index + '_true'}">사용</label>
                        <input type="radio" th:name="${'active_' + status.index}" value="false" th:checked="*{!active}" th:id="${'active_' + status.index + '_false'}">
                        <label th:for="${'active_' + status.index + '_false'}">미사용</label>
                    </td>
                    <td>
                        <a th:href="@{/admin/banner/edit/{seq}(seq=*{seq})}" class="sbtn">
                            <i class="xi-pen"></i> 수정하기
                        </a>
                    </td>
                </tr>
            </tbody>
        </table>
        <div class="table_actions" th:unless="${items == null || items.isEmpty()}">
            <button type="button" class="form_action sbtn" data-mode="delete" data-form-name="frmList">
                선택 배너 삭제하기
            </button>
            <button type="button" class="form_action sbtn on" data-mode="edit" data-form-name="frmList">
                선택 배너 수정하기
            </button>
        </div>
    </form>
</section>
</html>
```

## 편의 메서드 추가 

> commons/Utils.java

```java
...

public class Utils {
    ...

    private final BannerInfoService bannerInfoService;
    
    ...
    
    /**
     * 배너 목록
     *
     * @param groupCode : 배너 그룹 코드
     * @return
     */
    public List<Banner> getBanners(String groupCode) {
        return bannerInfoService.getList(groupCode);
    }
    
}
```


## 사용 방법 


```properties
...
    <div class="banners" th:each="banner : ${@utils.getBanners('mainBanner1')}" th:object="${banner}">
        <a th:if="*{bannerImage != null}" th:href="*{bLink}" th:target="*{target}">
            <img th:src="*{bannerImage.fileUrl}">
        </a>
    </div>
...
```
