package com.label.practice.files.trans.utils;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.util.Optional;

public class FileHelper {
    public static String getLastModifiedTime(File file,String pattern) throws IOException {
        BasicFileAttributeView basicview = Files.getFileAttributeView(file.toPath(), BasicFileAttributeView.class,
                LinkOption.NOFOLLOW_LINKS);
        FileTime time = basicview.readAttributes().lastModifiedTime();
        long millis = time.toMillis();
        return new LocalDateTime(millis).toString(StringUtils.defaultString(pattern,"yyyy-MM-dd HH:mm:ss"));
    }

    public static File createDirIfIsNotExists(String pathname) {
        if (StringUtils.isBlank(pathname)) {
            throw new RuntimeException(String.format("%s不能为空", pathname));
        }
        File file = new File(pathname);
        if (!file.exists()) {
            System.out.println("!file.exists():%s.创建该文件夹" + pathname);
            file.mkdirs();
        }

        return file;
    }

    public static String getFileType(File file) {
        return Optional.ofNullable(file)
                .map(File::getName)
                .map(name -> StringUtils.substringAfterLast(name, "."))
                .map(type -> type.toUpperCase()).orElse(StringUtils.EMPTY);
    }
}
