package org.choongang.member.service;

import org.choongang.commons.Utils;
import org.choongang.commons.exceptions.CommonException;
import org.springframework.http.HttpStatus;

public class MemberNotFoundException extends CommonException {
    public MemberNotFoundException() {
        super(Utils.getMessage("NotFound.member"), HttpStatus.NOT_FOUND);
    }
}
