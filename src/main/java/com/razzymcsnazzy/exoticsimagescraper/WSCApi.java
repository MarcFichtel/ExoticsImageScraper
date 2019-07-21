package com.razzymcsnazzy.exoticsimagescraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class for scraping tarantula species and their scientific synonyms
 *   off of the World Spider Catalog.
 *
 * Tried to use their official API, but had issues around finding LSIDs
 *   needed to make requests. See https://wsc.nmbe.ch/dataresources.
 */
class WSCApi {
  private static final String WSC_ROOT = "https://wsc.nmbe.ch";
  private static final String THERAPOSIDAE_GENERA_URL = "/genlist/100/Theraphosidae";

  /**
   * Map tarantula species to their synonyms.
   * @return the map
   * @throws IOException you're no good, duck
   */
  TreeMap<String, List<String>> getAllSpeciesWihSynonyms() throws IOException {
    final TreeMap<String, List<String>> allSpeciesWithSynonyms = new TreeMap<>();
    final Map<String, String> genusToSpeciesUrl = getTarantulaGeneraWithSpeciesUrl();
    for (Map.Entry genus : genusToSpeciesUrl.entrySet()) {
      final Map<String, List<String>> speciesWithSynonyms = getSpeciesWithSynonymsInGenus(genus.getValue().toString());
      allSpeciesWithSynonyms.putAll(speciesWithSynonyms);
    }
    System.out.println("Found " + allSpeciesWithSynonyms.size() + " tarantula species");
    return allSpeciesWithSynonyms;
  }

  /**
   * Scrape WSC for tarantula genera and the URL to their species list.
   * @return the genera mapped to the URL of their list of species
   * @throws IOException if there's a boo boo
   */
  private Map<String, String> getTarantulaGeneraWithSpeciesUrl() throws IOException {
    final Map<String, String> genusToLsid = new TreeMap<>();
    final Document generaTablePage = Jsoup.connect(WSC_ROOT + THERAPOSIDAE_GENERA_URL).get();
    final Elements generaTableRows = generaTablePage.getElementsByTag("tr");
    for (int i = 1; i < generaTableRows.size(); i++) {
      final Element row = generaTableRows.get(i);
      final String genus = row.getElementsByTag("i").html();
      final String speciesUrl = row.getElementsByAttributeValue("title", "Show species entries").attr("href");
      genusToLsid.put(genus, speciesUrl);
    }
    return genusToLsid;
  }

  /**
   * Get all species in a genus.
   * @param url to species list of a genus
   * @return each species in the genus mapped to a list of its synonyms
   * @throws IOException aww boo
   */
  private Map<String, List<String>> getSpeciesWithSynonymsInGenus(final String url) throws IOException {
    final Map<String, List<String>> speciesWithSynonyms = new TreeMap<>();
    final Document genusCatalogPage = Jsoup.connect(WSC_ROOT + url).get();
    final Elements species = genusCatalogPage.getElementsByClass("speciesTitle");

    // get species
    for (Element sp : species) {
      final String speciesName = sp.getElementsByTag("i").first().html();
      speciesWithSynonyms.put(speciesName, new ArrayList<>());
    }

    // get synonyms
    for (Element sp : species) {
      final Elements synonymsContainer = sp.nextElementSibling().getElementsByTag("i");
      for (Element synonym : synonymsContainer) {
        final String speciesName = sp.getElementsByTag("i").first().html();

        // issue: not all of these may refer to actual synonyms
        if (synonym.html().split(" ").length == 1 ||
          synonym.html().equals(speciesName) ||
          speciesWithSynonyms.keySet().contains(synonym.html()) ||
          speciesWithSynonyms.get(speciesName).contains(synonym.html()))
          continue;

        speciesWithSynonyms.get(speciesName).add(synonym.html());
      }
    }

    return speciesWithSynonyms;
  }
}
