package com.razzymcsnazzy.exoticsimagescraper;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

/**
 * Class for doing any processing on the scraped images that
 *   is necessary for them to be usable in the classifier model.
 */
class PostProcessor {

  private PostProcessor() { }

  /**
   * Copy image dirs with a certain number of images into new dirs.
   * GCP's AutoML Vision requires at least 10 images per label, and
   *   works best with at least 100 images per label. Extracting the
   *   labels with a certain minimum of images allows us to easily
   *   create models trained on labels with the specified minimum of
   *   images (i.e. 10, 50, 100, 500, 1000, etc).
   *
   * @param min number of images in labels to be copied
   */
  static void copyLabelsWithMinImages(final Integer min) {
    try {
      System.out.println("Extracting species with at least " + min + " images");

      // get image root directory
      final String projectRoot = System.getProperty("user.dir");
      final File allImages = new File(projectRoot + "\\images\\all");
      final File newImages = new File(projectRoot + "\\images\\new");


      if (!allImages.exists() || Objects.requireNonNull(allImages.listFiles()).length == 0) {
        throw new IllegalArgumentException("There is no images\\all folder");
      }
      if (!newImages.exists() || Objects.requireNonNull(newImages.listFiles()).length == 0) {
        throw new IllegalArgumentException("There is no images\\new folder");
      }

      // copy image directory
      final File subsetDir = new File(projectRoot + "\\images\\min" + min);
      subsetDir.mkdir();
      FileUtils.copyDirectory(allImages, subsetDir);

      // remove folders with less than minimum images
      for (File dir : Objects.requireNonNull(subsetDir.listFiles())) {
        if (Objects.requireNonNull(dir.listFiles()).length < min) {
          for (File file : Objects.requireNonNull(dir.listFiles())) {
            Files.delete(file.toPath());
          }
          Files.delete(dir.toPath());
        }
      }
    } catch (IOException e) {
      System.err.println("An unexpected error occurred extracting species with a minimum of " + min + " images: " + e);
    }
  }
}
