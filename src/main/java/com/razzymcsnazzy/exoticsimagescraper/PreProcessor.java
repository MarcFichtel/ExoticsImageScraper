package com.razzymcsnazzy.exoticsimagescraper;

import java.io.File;
import java.util.List;

/**
 * Class for doing any preparation that is necessary for scraping images.
 */
class PreProcessor {

  private PreProcessor() { }

  /**
   * Make image directories for all species.
   * @param species to make directories for
   */
  static void makeSpeciesDirectories(final List<String> species) {
    final String projectRoot = System.getProperty("user.dir");
    final File root = new File(projectRoot + "\\images");
    final File allImages = new File(root + "\\all");
    final File newImages = new File(root + "\\new");
    root.mkdir();
    allImages.mkdir();
    newImages.mkdir();

    for (String s : species) {
      new File(allImages + "\\" + s).mkdir();
      new File(newImages + "\\" + s).mkdir();
    }
  }
}
