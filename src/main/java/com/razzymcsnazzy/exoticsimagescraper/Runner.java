package com.razzymcsnazzy.exoticsimagescraper;

import com.razzymcsnazzy.exoticsimagescraper.scrapers.ScrapeMaster;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Run the scraper.
 *
 *  TODO
 *    - more sources
 *    - download new images to separate folder
 *    - do not scrape synonym if its equal to an accepted species name
 *    - how to deal with B hamorii vs. B smithi (many images mislabeled)?
 *    - is there a good way to differentiate by gender?
 */
public class Runner {
  public static void main(final String[] args) {
    try {

      // get species with synonyms from world spider catalog
      final WSCApi wscApi = new WSCApi();
      final TreeMap<String, List<String>> allSpeciesWithSynonyms = wscApi.getAllSpeciesWihSynonyms();

      // create image directories
      PreProcessor.makeSpeciesDirectories(new ArrayList<>(allSpeciesWithSynonyms.keySet()));

      // scrape images off arachnoboards tags
      new ScrapeMaster(allSpeciesWithSynonyms).scrapeABTags();

      // extract images in directories with a minimum of images
      PostProcessor.copyLabelsWithMinImages(10);
      PostProcessor.copyLabelsWithMinImages(100);
    }
    catch (Exception e) {
      System.err.println("An unexpected error occurred: " + e.getMessage());
    }
  }
}
