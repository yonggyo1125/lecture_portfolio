package org.choongang.file;

import org.choongang.file.service.FileInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

@SpringBootTest
public class ThumbnailTest {
    @Autowired
    private FileInfoService infoService;

    @Test
    void getThumbTest() {
        String[] data = infoService.getThumb(502L, 250, 250);
        System.out.println(Arrays.toString(data));
    }
}
