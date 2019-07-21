package com.razzymcsnazzy.exoticsimagescraper.scrapers;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;

/**
 * Worker used for scraping images off Arachnoboards tags.
 */
public class ABTagsScrapeWorker extends ScrapeWorker {

  /**
   * Scrape all images off AB in a tag for a species.
   * @param speciesLabel to scrape (scientific name)
   * @param speciesNames to scrape (scientific name and synonyms)
   * @param speciesToShutdownOn to shut down scraper on
   */
  ABTagsScrapeWorker(final String speciesLabel, final List<String> speciesNames, final String speciesToShutdownOn) {
    super("http://arachnoboards.com/", speciesLabel, speciesNames, speciesToShutdownOn);
  }

  /**
   * Scrape images.
   */
  public void run() {
    System.out.println("(" + Thread.currentThread().getName() + ") Scraping " + speciesLabel);

    // scrape each name
    for (String species : speciesNames) {
      imagesPerName.put(species, 0);
      Document currentPage = getABTagsPage(species);

      // scrape each tags page
      while (!isNull(currentPage)) {
        final Elements thumbnails = currentPage.getElementsByClass("listBlock mediaThumb");

        // scrape image corresponding to each tag
        for (Element thumbnail : thumbnails) {
          final Document thumbnailGalleryPage = getPage(thumbnail.getElementsByTag("a").get(0).attr("href") + "/");

          // if page contains tag sexing, skip (not interested in ventral or exuviae images)
          // can also exclude other tags here like enclosure, but many enclosure pics also contain a T
          final Elements imageTags = thumbnailGalleryPage.getElementsByClass("tagList").get(0).children();
          boolean skip = false;
          for (Element imageTag : imageTags) {
            if (imageTag.childNodeSize() > 0 && imageTag.child(0).hasAttr("href")) {
              final String tagValue = imageTag.child(0).attr("href");
              if (tagValue.contains("sexing")) {
                skip = true;
                break;
              }
            }
          }
          if (skip) continue;

          // get image
          final Element imageContainer = thumbnailGalleryPage
            .getElementsByClass("imageContainer")
            .first();
          final String imagePath = imageContainer.getElementsByTag("img").first().attr("src");

          // download, if not already present
          final Boolean downloaded = downloadImage(imageDirExisting.getPath(), imageDirNew.getPath(), imagePath);
          if (downloaded) {
            numImages++;
            imagesPerName.put(species, imagesPerName.get(species) + 1);
          }
        }

        // next page
        currentPage = nextABPage(currentPage);
      }
    }

    // delete directory, if there are no images
    try {
      if (imageDirExisting.exists() && Objects.requireNonNull(imageDirExisting.listFiles()).length == 0) Files.delete(imageDirExisting.toPath());
    } catch (IOException e) {
      System.err.println("Error deleting empty directory " + imageDirExisting.getPath() + ": " + e);
    }

    // show progress
    this.printProgress(speciesLabel, numImages, numExisting, imagesPerName);
    ScrapeMaster.incrementNumSpecies();

    // set scraper shutdown flag on last species
    if (speciesNames.get(speciesNames.size() - 1).equals(speciesToShutdownOn))
      ScrapeMaster.shutdown();
  }

  /**
   * Get an Arachnoboards tags page.
   * @param species tag to get page of
   * @return the page
   */
  private Document getABTagsPage(final String species) {
    return getPage("tags/" + species.replace(" ", "-").toLowerCase() + "/");
  }

  /**
   * If a gallery page has any more pages following it, get the next page.
   * @param page the current page
   * @return the next page
   */
  private Document nextABPage(final Document page) {
    try {
      final Element nextPage = page.getElementsContainingOwnText("Next").get(0);
      final String nextPageUrl = nextPage.attr("href") + "/";
      return getPage(nextPageUrl);
    }

    // there is no next page
    catch (IndexOutOfBoundsException e) {
      return null;
    }
  }
}
