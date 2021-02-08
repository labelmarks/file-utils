package com.label.practice.files.trans;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.label.practice.files.trans.utils.FileHelper;
import com.label.practice.files.trans.utils.StringHelper;
import java.util.Optional;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/** Hcello world! */
public class Trans {

  public static String TO_PATH_BASE = "D:\\USFUL\\media";
  public static final String TO_PATH_IMAGE = "image";
  public static final String TO_PATH_VEDIO = "vedio";
  public static String FROM_PATH = "C:\\Users\\916an\\Desktop\\新建文件夹";
  public static String BACK_PATH = "C:\\Users\\916an\\Desktop\\新建文件夹_bak";
  private static final String[] IMAGE_LIST =
      StringUtils.split(
          "bmp,jpg,jpeg,png,tiff,gif,pcx,tga,exif,fpx,svg,psd,cdr,pcd,dxf,ufo,eps,ai,raw,WMF,webp",
          ",");
  private static final String[] VEDIO_LIST = StringUtils.split("MP4/3GP/MPG/AVI/WMV/FLV/SWF", "/");
  private static Set<String> existsFileMD5Set = Sets.newConcurrentHashSet();
  private static Map<String, String> exists = Maps.newConcurrentMap();

  private static AtomicInteger transCounter = new AtomicInteger();

  private static AtomicBoolean IS_NEED_BACKUP = new AtomicBoolean(true);

  public static void main(String[] args) throws IOException {
    System.out.println("参数：FROM_PATH TO_PATH_BASE IS_NEED_BACKUP");
    //        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "30");
    if (ArrayUtils.isNotEmpty(args) && ArrayUtils.getLength(args) >= 2) {
      FROM_PATH = args[0];
      BACK_PATH = FROM_PATH + "_back";
      TO_PATH_BASE = args[1];
    }
    if (ArrayUtils.getLength(args) >= 3) {
      IS_NEED_BACKUP.set(BooleanUtils.toBoolean(args[2]));
    }
    loadExistsFiles(new File(TO_PATH_BASE));
    trans(new File(FROM_PATH), TO_PATH_BASE, BACK_PATH);

    System.out.println("已存在的记录：" + JSON.toJSONString(exists, true));
  }

  @SneakyThrows
  private static void trans(File file, String targetPathBase, String backPath) {
    if (null == file || !file.exists()) {
      return;
    }
    prepareDir(targetPathBase, backPath);
    if (file.isDirectory()) {
      transDir(file, targetPathBase, backPath);
    } else if (file.isFile()) {
      transFile(file, targetPathBase, backPath);
    }
  }

  private static void transFile(File file, String targetPathBase, String backPath)
      throws IOException {
    //        System.out.print(counter.incrementAndGet() + ",");
    String fileType = FileHelper.getFileType(file);

    String fileMD5 = getFileMD5(file);
    if (existsFileMD5Set.contains(fileMD5)) {
      exists.put(file.getName(), fileMD5);
      return;
    }

    File toFile = prapareTargetFile(targetPathBase, fileType, fileMD5, file);
    if (Objects.isNull(toFile)) {
      return;
    }
    moveIfNecessary(file, toFile);

    existsFileMD5Set.add(fileMD5);

    File backFile = null;
    if (IS_NEED_BACKUP.get()) {
      backFile = prepareBakFile(file, backPath, fileType, fileMD5);
      moveIfNecessary(file, backFile);
    }

    System.out.println(
        "["
            + transCounter.getAndIncrement()
            + "]\t"
            + file.getCanonicalPath()
            + (IS_NEED_BACKUP.get() ? "\t备份到" + backFile.getCanonicalPath() : "")
            + "\t移动到"
            + toFile.getCanonicalPath());
    file.delete();
  }

  private static void transDir(File file, String targetPathBase, String backPath) {
    File[] files = file.listFiles();
    if (ArrayUtils.isEmpty(files)) {
      return;
    }
    Arrays.stream(files)
        .parallel()
        .forEach(
            tmpFile -> {
              trans(tmpFile, targetPathBase, backPath);
            });
  }

  private static void prepareDir(String targetPathBase, String backPath) {
    FileHelper.createDirIfIsNotExists(backPath);
    FileHelper.createDirIfIsNotExists(targetPathBase);
    FileHelper.createDirIfIsNotExists(targetPathBase + File.separator + TO_PATH_IMAGE);
    FileHelper.createDirIfIsNotExists(targetPathBase + File.separator + TO_PATH_VEDIO);
  }

  @SneakyThrows
  private static File prapareTargetFile(
      String targetPathBase, String fileType, String fileMD5, File file) {
    String targetSubDirPath = getTargetSubDirPath(file);
    if (StringUtils.isBlank(targetSubDirPath)) {
      return null;
    }

    // 如果文件本身携带时间，取文件名的时间(支持格式见方法)
    String fileDateTimeStr =
        Optional.ofNullable(StringHelper.getDateTime(file.getName()))
            .map(e -> e.toString(DatePattern.PURE_DATETIME_PATTERN))
            .orElse(FileHelper.getLastModifiedTime(file, DatePattern.PURE_DATETIME_PATTERN));
    String targetFilePath =
        MessageFormat.format(
            "{0}" + File.separator + "{1}" + File.separator + "{2}-{3}.{4}",
            targetPathBase,
            targetSubDirPath,
            fileDateTimeStr,
            fileMD5,
            fileType);
    return new File((targetFilePath));
  }

  private static File prepareBakFile(File file, String backPath, String fileType, String fileMD5) {
    File backFile = null;
    try {
      backFile = new File((backPath + File.separator + file.getName()));
    } catch (Exception e) {
      backFile = new File((backPath + File.separator + fileMD5 + "." + fileType));
    }
    return backFile;
  }

  private static void moveIfNecessary(File fromFile, File toFile) throws IOException {
    if (null == fromFile || !fromFile.exists()) {
      return;
    }
    if (null == toFile || toFile.exists()) {
      return;
    }
    Files.copy(fromFile, toFile);
  }

  private static String getTargetSubDirPath(File file) {
    String realType = FileHelper.getFileType(file);

    if (StringUtils.equalsAnyIgnoreCase(realType, IMAGE_LIST)) {
      return TO_PATH_IMAGE;
    } else if (StringUtils.equalsAnyIgnoreCase(realType, VEDIO_LIST)) {
      return TO_PATH_VEDIO;
    }
    return StringUtils.EMPTY;
  }

  @SneakyThrows
  private static void loadExistsFiles(File file) {
    if (null == file || !file.exists()) {
      return;
    }
    if (file.isDirectory()) {
      File[] files = file.listFiles();
      if (ArrayUtils.isEmpty(files)) {
        return;
      }
      Arrays.stream(files).parallel().forEach(tmpFile -> loadExistsFiles(tmpFile));
    } else if (file.isFile()) {
      existsFileMD5Set.add(getFileMD5(file));
    }
  }

  private static String getFileMD5(File file) throws IOException {
    Set<String> md5s = StringHelper.listRegexes(file.getName(), StringHelper.MD5_PATTERN);
    if (!md5s.isEmpty()) {
      return md5s.stream().findAny().get();
    }
    return DigestUtils.md5Hex(Files.toByteArray(file));
  }
}
