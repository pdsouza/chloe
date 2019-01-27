# Chloe

A simple, composable static site generator for Clojurists.

**chloe is in the very early stages right now and documentation / examples are sparse—this will improve soon!**

## Usage

Chloe is designed to be un-opinionated: it's basically a lean set of small, yet sharp, functions that make building static sites in Clojure a joy. That being said, here are some common usages of Chloe.

### Structure

Websites map very nicely to directory structures, so let's create one that represents your site.

Here's a sample layout:

```
resources/
  partials/
    post/
      chloe-is-awesome.md
      chloe-is-cool.md
  public/
    main.css
    images/
      chloe-architecture.jpg
src/
  my-site/
    page/
      about.clj
      index.clj
    core.clj
    layouts.clj
```

In this layout, partial content (simple text-focused content that will likely be transformed, laid out, etc. like markdown blog posts for example) is placed in `resources/partials` and assets (stuff that will be reflected exactly over to your site without any intermediate transforms) are placed in `resources/public`.

Notice how this structure is exactly the same as a standard Clojure project. That means you can work on your website using all the Clojure tooling that you know and love, like Leiningen.

### Build

Now, let's tap into Chloe to build your site:

```clojure
(ns my-site.core
  (:require [chloe.core :as chloe]
            [chloe.plugin.frontmatter :refer :all]
            [chloe.plugin.drafts :refer :all]
            [chloe.plugin.markdown :refer :all]
            [chloe.plugin.pretty-urls :refer :all]
            [chloe.plugin.pages :refer :all]
            [chloe.plugin.layout :refer :all])
  (:require [my-site.page.index :as index]
            [my-site.page.about :as about]
            [my-site.layouts :as layouts]))

(defn build []
  (->> (chloe/content "resources/partials")
       (merge (chloe/assets "resources/public"))
       (frontmatter)
       (remove-drafts)
       (markdown)
       (prettify-urls)
       (add-pages {"/" index/render
                   "/about/" about/render})
       (layout {"/post/.*" layouts/post
                ".*" layouts/default})))
```

As you can see, building your site is as simple as threading together Chloe functions that transform your site's content.

### Export

When you want to export your site, just use `chloe/export`:

```clojure
(defn export []
  (->> (build)
       (chloe/export "out")))
```

### Develop Live

For live development, you can set up a Ring handler by passing your build function to `chloe/ring-serve`:

```clojure
(def dev (chloe/ring-serve build))
```

## License

Copyright © 2019 Preetam J. D'Souza

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
