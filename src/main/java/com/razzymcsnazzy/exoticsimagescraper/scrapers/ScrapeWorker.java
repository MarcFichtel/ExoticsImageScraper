package com.razzymcsnazzy.exoticsimagescraper.scrapers;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

abstract class ScrapeWorker implements Runnable {

  /**
   * Root URL.
   */
  private String root;

  /**
   * Label of tarantula species to tag images with.
   */
  String speciesLabel;

  /**
   * Names of species to scrape images for (scientific name and synonyms).
   */
  List<String> speciesNames;

  /**
   * When this species is scraped, tell scraper to shut down.
   */
  String speciesToShutdownOn;

  /**
   * Directory to check for existing images.
   */
  File imageDirExisting;

  /**
   * Directory to scrape new images into.
   */
  File imageDirNew;

  /**
   * Number of images scraped.
   */
  Integer numImages;

  /**
   * Number of images that have been scraped previously.
   */
  Integer numExisting;

  /**
   * How many images have been scraped per name.
   */
  TreeMap<String, Integer> imagesPerName;

  ScrapeWorker(final String root, final String speciesLabel, final List<String> speciesNames, final String speciesToShutdownOn) {
    this.root = root;
    this.speciesLabel = speciesLabel;
    this.speciesNames = speciesNames;
    this.speciesToShutdownOn = speciesToShutdownOn;
    this.imageDirExisting = new File(System.getProperty("user.dir") + "\\images\\all\\" + speciesLabel);
    this.imageDirNew = new File(System.getProperty("user.dir") + "\\images\\new\\" + speciesLabel);
    this.numImages = 0;
    this.numExisting = imageDirExisting.exists() ? Objects.requireNonNull(imageDirExisting.listFiles()).length : 0;
    this.imagesPerName = new TreeMap<>();
  }

  /**
   * Get a page.
   * @param path of page to get
   * @return the page
   */
  Document getPage(final String path) {
    final String url = root + path;
    try {
      return Jsoup.connect(url).timeout(999999999).get();
    } catch (HttpStatusException e) {
      return null;
    } catch (IOException e) {
      System.err.println("Error getting " + url + ": " + e.getMessage());
      return null;
    }
  }

  /**
   * Print scraping progress.
   * @param name the currently accepted scientific name
   * @param numImages scraped in total by this worker
   * @param imagesPerName scraped per name (i.e. for synonyms)
   */
  void printProgress(final String name, final Integer numImages, final Integer numExisting, final TreeMap<String, Integer> imagesPerName) {
    if (numImages + numExisting > 0) {
      final StringBuilder whatHaveWeScraped = new StringBuilder(String.format(
        "(%s) Scraped %d new images for %s off %s (%d previously present)",
        Thread.currentThread().getName(), numImages, name, root, numExisting));

      if (numImages > 0) {
        for (Map.Entry e : imagesPerName.entrySet()) {
          whatHaveWeScraped
            .append("\n\t* ")
            .append(e.getKey())
            .append(": ")
            .append(e.getValue());
        }
      }
      System.out.println(whatHaveWeScraped.toString());
    }
  }

  /**
   * Download an image.
   * See https://stackoverflow.com/questions/12465586/how-can-i-download-an-image-using-jsoup.
   * @param directoryExisting path to check if image already exists
   * @param directoryNew path to download image to, if it doesn't already exist
   * @param imageUrl to download image from
   */
  Boolean downloadImage(final String directoryExisting, final String directoryNew, final String imageUrl) {
    try {
      final String url = root + imageUrl;
      final Connection.Response image = Jsoup.connect(url)
          .timeout(Integer.MAX_VALUE)
          .ignoreContentType(true)
          .maxBodySize(Integer.MAX_VALUE)
          .execute();
      final String extension = image.contentType().split("/")[1];
      final String pathExisting = directoryExisting + "\\" + imageUrl.split("/")[1].replace(".", "_") + "." + extension;
      final String pathNew = directoryNew + "\\" + imageUrl.split("/")[1].replace(".", "_") + "." + extension;

      // don't download if image is already present
      if (new File(pathExisting).exists() || pathExisting.contains("all-my-ts_56010")) return false;

      // download
      final FileOutputStream out = new FileOutputStream(new File(pathNew));
      out.write(image.bodyAsBytes());
      out.close();
    } catch (HttpStatusException e) {
      System.err.println("404 image not found: " + imageUrl);
    } catch (IOException e) {
      System.err.println("An unexpected error occurred: " + e.getMessage());
    }
    return true;
  }
}
