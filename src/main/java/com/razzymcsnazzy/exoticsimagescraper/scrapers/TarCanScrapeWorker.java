package com.razzymcsnazzy.exoticsimagescraper.scrapers;

import java.util.List;

/**
 * Worker used for scraping images off the Tarantula Canada gallery.
 */
public class TarCanScrapeWorker extends ScrapeWorker {

  /**
   * Tarantula Canada Gallery URL.
   */
  private static final String TARCAN_GALLERY_URL = "info_en.php?page=gallery";

  /**
   * Scrape all images off TarCan gallery for a species.
   * @param speciesLabel to scrape (scientific name)
   * @param speciesNames to scrape (scientific name and synonyms)
   * @param speciesToShutdownOn to shut down scraper on
   */
  public TarCanScrapeWorker(final String speciesLabel, final List<String> speciesNames, final String speciesToShutdownOn) {
    super("https://www.tarantulacanada.ca/", speciesLabel, speciesNames, speciesToShutdownOn);
  }

  /**
   * Scrape images.
   */
  @Override
  public void run() {

  }
}
