# Tarantula Image Scraper
A small application that gets and labels tarantulas by species.
The labeled images can then be used to train an ML model to classify animals in new images.

# Scraping Algorithm
1) Get all currently accepted scientific names of tarantula species from the World Spider Catalog
2) Get all formerly accepted scientific synonyms, as well
3) Scrape Arachnoboards tags for images of each species + synonyms and label as the currently accepted scientific name
4) Exclude unsuitable images (i.e. molts, enclosures, ventral shots, ...)
5) Make separate directories that contain only those species with at least 10 and 100 images

# TODOs
* Is there a way to include gender?
* Scrape more sources
* Retrain model once there are more species with 100+ images
