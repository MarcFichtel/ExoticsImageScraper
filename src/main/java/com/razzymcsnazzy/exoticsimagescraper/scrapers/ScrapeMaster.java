package com.razzymcsnazzy.exoticsimagescraper.scrapers;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Small app to scrape tarantula images off of
 * - Arachnoboards tags
 *
 * Images are categorized by species. NB the scraper also scrapes
 *   images for scientific synonyms and labels them as the currently
 *   accepted scientific name. The source of accepted scientific
 *   names and synonyms is the world spider catalog (see WSCApi).
 */
public class ScrapeMaster {

  /**
   * Shutdown flag to be set when last species is being scraped.
   */
  private static Boolean shutdownScraper = false;

  /**
   * Counter for number of species actually scraped.
   */
  private static Integer numSpecies = 0;

  /**
   * Tarantula species with synonyms.
   */
  private Map<String, List<String>> speciesWithSynonyms;

  /**
   * The last species name in speciesWithSynonyms
   */
  private String lastSpeciesName;

  /**
   * Constructor.
   */
  public ScrapeMaster(final SortedMap<String, List<String>> speciesWithSynonyms) {
    this.speciesWithSynonyms = speciesWithSynonyms;

    // get the last species name from the map
    final String lastSpecies = speciesWithSynonyms.lastKey();
    final List<String> synonyms = speciesWithSynonyms.get(lastSpecies);
    this.lastSpeciesName = synonyms.isEmpty() ? lastSpecies : synonyms.get(synonyms.size() - 1);
  }

  /**
   * Set shutdown flag.
   */
  static void shutdown() { shutdownScraper = true; }


  /**
   * Increment number of species scraped.
   */
  static void incrementNumSpecies() { numSpecies++; }

  /**
   * Scrape images for each species (incl. synonyms) from Arachnoboards tags.
   */
  public void scrapeABTags() {
    System.out.println("Start scraping at " + new Date());

    final ExecutorService scraper = Executors.newFixedThreadPool(10, new ThreadFactoryBuilder().setNameFormat("Scraper Thread %d").build());
    for (String species : speciesWithSynonyms.keySet()) {
      final List<String> nameAndSynonyms = new ArrayList<>();
      nameAndSynonyms.add(species);
      nameAndSynonyms.addAll(speciesWithSynonyms.get(species));
      scraper.execute(new ABTagsScrapeWorker(species, nameAndSynonyms, lastSpeciesName));
    }

    // await scraper shutdown
    while (!shutdownScraper) {
      try { Thread.sleep(10000); }
      catch (InterruptedException e) {
        System.err.println("Main thread interrupted: " + e.getMessage());
      }
    }
    scraper.shutdown();

    // await scraper termination
    while (!scraper.isTerminated()) {
      try { Thread.sleep(10000); }
      catch (InterruptedException e) {
        System.err.println("Main thread interrupted: " + e.getMessage());
      }
    }
    System.out.println("Done scraping " + numSpecies + " at " + new Date());
  }
}
