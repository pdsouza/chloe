# chloe

A simple, composable static site generator for Clojurists.

**chloe is in the very early stages right now and documentation / examples are sparse—this will improve soon!**

## Usage

*Note: Chloe is designed to be un-opinionated—it's basically a bunch of small, yet sharp, functions that make building static sites in Clojure a joy. Just keep in mind that you may discover better ways of using Chloe in your projects than what is listed here.*

To get started, create a directory structure that represents your site:

```
resources/
  partials/
    post/
      chloe-is-awesome.md
      clojure-notes.md
    public/
      main.css
src/
  my-site/
    page/
      about.clj
      index.clj
    core.clj
    layouts.clj
```

Notice how this structure is exactly the same as a standard Clojure project. That means you can work on your website using all the Clojure tooling that you know and love, like Leiningen.

The only real requirement at the moment is that your partial content (simple content that will be transformed, laid out, etc. like markdown blog posts for example) is placed in `resources/partials` and your assets are placed in `resources/public`.

Next, make use of Chloe to build your site:

```clojure
(defn build []
  (->> (chloe/get-sitemap)
       (frontmatter)
       (remove-drafts)
       (markdown)
       (prettify-urls)
       (add-pages {"/" index/render
                   "/about/" about/render})
       (layout {"/post/.*" layouts/post
                ".*" layouts/default})))
```

When you want to export your site, just use `chloe/export`:

```clojure
(defn export-site []
  (->> (build)
       (chloe/export "out")))
```

For live development, you can set up a Ring handler by passing your build function to `chloe/ring-serve`:

```clojure
(def app (chloe/ring-serve build))
```

## License

Copyright © 2019 Preetam J. D'Souza

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
